package com.spendwise.unittest;

import com.spendwise.dto.BudgetDTO;
import com.spendwise.dto.BudgetFilterDTO;
import com.spendwise.dto.RecurrentExpenseDTO;
import com.spendwise.model.Budget;
import com.spendwise.model.RecurrentExpense;
import com.spendwise.model.RecurrentExpenseRecord;
import com.spendwise.model.auth.User;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.RecurrentExpenseRecordRepository;
import com.spendwise.repository.RecurrentExpenseRepository;
import com.spendwise.service.BudgetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("Budget Unit Tests")
public class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private RecurrentExpenseRepository recurrentExpenseRepository;

    @Mock
    private RecurrentExpenseRecordRepository recurrentExpenseRecordRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;

    private static RecurrentExpense re1;
    private static RecurrentExpense re2;

    @BeforeAll
    static void init() {
        re1 = new RecurrentExpense();
        re1.setId(1L);
        re1.setDescription("Alquiler");
        re1.setAmountInPesos(new BigDecimal("100000"));
        re1.setAmountInDollars(new BigDecimal("80"));
        re1.setEnabled(true);

        re2 = new RecurrentExpense();
        re2.setId(2L);
        re2.setDescription("Expensas");
        re2.setAmountInPesos(new BigDecimal("50000"));
        re2.setAmountInDollars(new BigDecimal("40"));
        re2.setEnabled(true);
    }

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test");
        testUser.setEnabled(true);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Create budget sets enabled to true and saves")
    public void testCreate() {
        BudgetDTO dto = new BudgetDTO();
        dto.setDescription("Budget Enero 2025");
        dto.setMonth(1);
        dto.setYear(2025);
        dto.setRecurrentExpenses(Collections.emptyList());

        Mockito.when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        BudgetDTO result = budgetService.create(dto);

        assertNotNull(result);
        assertEquals("Budget Enero 2025", result.getDescription());
        assertEquals(1, result.getMonth());
        assertEquals(2025, result.getYear());
        assertTrue(result.getEnabled());
        assertEquals(BigDecimal.ZERO, result.getTotalExpectedARS());
        assertEquals(BigDecimal.ZERO, result.getTotalExpectedUSD());
        assertEquals(0, result.getPendingCount());
        assertEquals(0, result.getCancelledCount());
        Mockito.verify(budgetRepository).save(any(Budget.class));
        Mockito.verifyNoMoreInteractions(budgetRepository);
        Mockito.verifyNoInteractions(recurrentExpenseRepository);
        Mockito.verifyNoInteractions(recurrentExpenseRecordRepository);
    }

    @Test
    @DisplayName("Create budget with recurrent expenses resolves IDs and saves")
    public void testCreateWithRecurrentExpenses() {
        RecurrentExpenseDTO reDTO1 = new RecurrentExpenseDTO();
        reDTO1.setId(1L);
        RecurrentExpenseDTO reDTO2 = new RecurrentExpenseDTO();
        reDTO2.setId(2L);

        BudgetDTO dto = new BudgetDTO();
        dto.setDescription("Budget Febrero 2025");
        dto.setMonth(2);
        dto.setYear(2025);
        dto.setRecurrentExpenses(List.of(reDTO1, reDTO2));

        Mockito.when(recurrentExpenseRepository.findById(1L)).thenReturn(Optional.of(re1));
        Mockito.when(recurrentExpenseRepository.findById(2L)).thenReturn(Optional.of(re2));
        Mockito.when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> {
            Budget b = inv.getArgument(0);
            // simulate the resolved list being saved
            return b;
        });
        // Both records exist but are not cancelled for Feb 2025
        Mockito.when(recurrentExpenseRecordRepository.findByRecurrentExpenseAndMonthAndYear(re1, 2, 2025))
                .thenReturn(Optional.empty());
        Mockito.when(recurrentExpenseRecordRepository.findByRecurrentExpenseAndMonthAndYear(re2, 2, 2025))
                .thenReturn(Optional.empty());

        BudgetDTO result = budgetService.create(dto);

        assertNotNull(result);
        assertEquals("Budget Febrero 2025", result.getDescription());
        assertTrue(result.getEnabled());
        assertEquals(2, result.getRecurrentExpenses().size());
        assertEquals(new BigDecimal("150000"), result.getTotalExpectedARS());
        assertEquals(new BigDecimal("120"), result.getTotalExpectedUSD());
        assertEquals(BigDecimal.ZERO, result.getTotalCancelledARS());
        assertEquals(0, result.getCancelledCount());
        assertEquals(2, result.getPendingCount());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FIND BY ID
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Find budget by ID returns it with calculated totals")
    public void testFindById() throws ChangeSetPersister.NotFoundException {
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setDescription("Budget Enero 2025");
        budget.setMonth(1);
        budget.setYear(2025);
        budget.setEnabled(true);
        budget.setUser(testUser);
        budget.setRecurrentExpenses(List.of(re1, re2));

        Mockito.when(budgetRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(budget));

        // re1 has a cancelled record for Jan 2025
        RecurrentExpenseRecord record1 = new RecurrentExpenseRecord();
        record1.setId(1L);
        record1.setCancelled(true);
        record1.setRecurrentExpense(re1);
        Mockito.when(recurrentExpenseRecordRepository.findByRecurrentExpenseAndMonthAndYear(re1, 1, 2025))
                .thenReturn(Optional.of(record1));

        // re2 has no record for Jan 2025
        Mockito.when(recurrentExpenseRecordRepository.findByRecurrentExpenseAndMonthAndYear(re2, 1, 2025))
                .thenReturn(Optional.empty());

        BudgetDTO result = budgetService.findById(1L);

        assertNotNull(result);
        assertEquals("Budget Enero 2025", result.getDescription());
        assertEquals(2, result.getRecurrentExpenses().size());
        // Expected totals: re1(100000) + re2(50000) = 150000 ARS, 80+40=120 USD
        assertEquals(new BigDecimal("150000"), result.getTotalExpectedARS());
        assertEquals(new BigDecimal("120"), result.getTotalExpectedUSD());
        // Cancelled totals: only re1 cancelled → 100000 ARS, 80 USD
        assertEquals(new BigDecimal("100000"), result.getTotalCancelledARS());
        assertEquals(new BigDecimal("80"), result.getTotalCancelledUSD());
        assertEquals(1, result.getCancelledCount());
        assertEquals(1, result.getPendingCount());
        Mockito.verify(budgetRepository).findByIdAndUser(1L, testUser);
        Mockito.verifyNoMoreInteractions(budgetRepository);
        Mockito.verify(recurrentExpenseRecordRepository).findByRecurrentExpenseAndMonthAndYear(re1, 1, 2025);
        Mockito.verify(recurrentExpenseRecordRepository).findByRecurrentExpenseAndMonthAndYear(re2, 1, 2025);
        Mockito.verifyNoMoreInteractions(recurrentExpenseRecordRepository);
    }

    @Test
    @DisplayName("Find budget by ID throws exception when not found")
    public void testFindNonExistingById() {
        Mockito.when(budgetRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> budgetService.findById(999L));
        Mockito.verify(budgetRepository).findByIdAndUser(999L, testUser);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LIST
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("List all budgets returns complete list")
    public void testList() {
        Budget b1 = new Budget();
        b1.setId(1L);
        b1.setDescription("Budget Enero 2025");
        b1.setMonth(1);
        b1.setYear(2025);
        b1.setEnabled(true);
        b1.setRecurrentExpenses(Collections.emptyList());

        Budget b2 = new Budget();
        b2.setId(2L);
        b2.setDescription("Budget Febrero 2025");
        b2.setMonth(2);
        b2.setYear(2025);
        b2.setEnabled(true);
        b2.setRecurrentExpenses(Collections.emptyList());

        Page<Budget> page = new PageImpl<>(List.of(b1, b2));
        Pageable pageable = PageRequest.of(0, 20);
        BudgetFilterDTO filters = new BudgetFilterDTO();

        Mockito.when(budgetRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<BudgetDTO> result = budgetService.list(filters, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Budget Enero 2025", result.getContent().get(0).getDescription());
        assertEquals("Budget Febrero 2025", result.getContent().get(1).getDescription());
        Mockito.verifyNoInteractions(recurrentExpenseRepository);
        Mockito.verifyNoInteractions(recurrentExpenseRecordRepository);
    }

    @Test
    @DisplayName("List budgets with month/year filter")
    public void testListWithMonthYearFilter() {
        Budget b1 = new Budget();
        b1.setId(1L);
        b1.setDescription("Budget Enero 2025");
        b1.setMonth(1);
        b1.setYear(2025);
        b1.setEnabled(true);
        b1.setRecurrentExpenses(Collections.emptyList());

        Page<Budget> page = new PageImpl<>(List.of(b1));
        Pageable pageable = PageRequest.of(0, 20);
        BudgetFilterDTO filters = new BudgetFilterDTO();
        filters.setMonth(1);
        filters.setYear(2025);

        Mockito.when(budgetRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<BudgetDTO> result = budgetService.list(filters, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().get(0).getMonth());
        assertEquals(2025, result.getContent().get(0).getYear());
    }

    @Test
    @DisplayName("List budgets with no results")
    public void testListWithNoResults() {
        Page<Budget> page = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 20);
        BudgetFilterDTO filters = new BudgetFilterDTO();

        Mockito.when(budgetRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<BudgetDTO> result = budgetService.list(filters, pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Update budget modifies description without changing enabled")
    public void testUpdate() throws ChangeSetPersister.NotFoundException {
        Long id = 1L;
        Budget budget = new Budget();
        budget.setId(id);
        budget.setDescription("Budget Enero 2025");
        budget.setMonth(1);
        budget.setYear(2025);
        budget.setEnabled(true);
        budget.setUser(testUser);
        budget.setRecurrentExpenses(new ArrayList<>());

        BudgetDTO updateDTO = new BudgetDTO();
        updateDTO.setDescription("Budget Enero 2025 Actualizado");
        updateDTO.setMonth(1);
        updateDTO.setYear(2025);
        updateDTO.setRecurrentExpenses(Collections.emptyList());

        Mockito.when(budgetRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(budget));
        Mockito.when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        BudgetDTO result = budgetService.update(id, updateDTO);

        assertNotNull(result);
        assertEquals("Budget Enero 2025 Actualizado", result.getDescription());
        assertTrue(result.getEnabled());
        Mockito.verify(budgetRepository).findByIdAndUser(id, testUser);
        Mockito.verify(budgetRepository).save(budget);
        Mockito.verifyNoMoreInteractions(budgetRepository);
        Mockito.verifyNoInteractions(recurrentExpenseRepository);
        Mockito.verifyNoInteractions(recurrentExpenseRecordRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Delete budget removes it from the database")
    public void testDelete() throws ChangeSetPersister.NotFoundException {
        Long id = 1L;
        Budget budget = new Budget();
        budget.setId(id);
        budget.setDescription("Budget Enero 2025");
        budget.setEnabled(true);
        budget.setUser(testUser);
        budget.setMonth(1);
        budget.setYear(2025);
        budget.setRecurrentExpenses(Collections.emptyList());

        Mockito.when(budgetRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(budget));

        BudgetDTO result = budgetService.delete(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        Mockito.verify(budgetRepository).findByIdAndUser(id, testUser);
        Mockito.verify(budgetRepository).delete(budget);
        Mockito.verifyNoMoreInteractions(budgetRepository);
        Mockito.verifyNoInteractions(recurrentExpenseRepository);
        Mockito.verifyNoInteractions(recurrentExpenseRecordRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ENABLE / DISABLE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Enable budget sets enabled to true")
    public void testEnable() throws ChangeSetPersister.NotFoundException {
        Long id = 1L;
        Budget budget = new Budget();
        budget.setId(id);
        budget.setDescription("Budget Enero 2025");
        budget.setEnabled(false);
        budget.setUser(testUser);
        budget.setMonth(1);
        budget.setYear(2025);
        budget.setRecurrentExpenses(Collections.emptyList());

        Mockito.when(budgetRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(budget));
        Mockito.when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        BudgetDTO result = budgetService.enable(id);

        assertTrue(result.getEnabled());
        Mockito.verify(budgetRepository).findByIdAndUser(id, testUser);
        Mockito.verify(budgetRepository).save(budget);
        Mockito.verifyNoMoreInteractions(budgetRepository);
    }

    @Test
    @DisplayName("Disable budget sets enabled to false")
    public void testDisable() throws ChangeSetPersister.NotFoundException {
        Long id = 1L;
        Budget budget = new Budget();
        budget.setId(id);
        budget.setDescription("Budget Enero 2025");
        budget.setEnabled(true);
        budget.setUser(testUser);
        budget.setMonth(1);
        budget.setYear(2025);
        budget.setRecurrentExpenses(Collections.emptyList());

        Mockito.when(budgetRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(budget));
        Mockito.when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        BudgetDTO result = budgetService.disable(id);

        assertFalse(result.getEnabled());
        Mockito.verify(budgetRepository).findByIdAndUser(id, testUser);
        Mockito.verify(budgetRepository).save(budget);
        Mockito.verifyNoMoreInteractions(budgetRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CREATE NEXT MONTH
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Create next month budget copies previous budget to next month")
    public void testCreateNextMonth() throws ChangeSetPersister.NotFoundException {
        Budget latestBudget = new Budget();
        latestBudget.setId(1L);
        latestBudget.setDescription("Presupuesto Mensual");
        latestBudget.setMonth(3);
        latestBudget.setYear(2026);
        latestBudget.setEnabled(true);
        latestBudget.setUser(testUser);
        latestBudget.setRecurrentExpenses(new ArrayList<>(List.of(re1, re2)));

        Mockito.when(budgetRepository.findTopByUserOrderByYearDescMonthDesc(testUser))
                .thenReturn(Optional.of(latestBudget));
        Mockito.when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(recurrentExpenseRecordRepository.findByRecurrentExpenseAndMonthAndYear(re1, 4, 2026))
                .thenReturn(Optional.empty());
        Mockito.when(recurrentExpenseRecordRepository.findByRecurrentExpenseAndMonthAndYear(re2, 4, 2026))
                .thenReturn(Optional.empty());

        BudgetDTO result = budgetService.createNextMonth();

        assertNotNull(result);
        assertEquals("Presupuesto Mensual", result.getDescription());
        assertEquals(4, result.getMonth());
        assertEquals(2026, result.getYear());
        assertTrue(result.getEnabled());
        assertEquals(2, result.getRecurrentExpenses().size());
        Mockito.verify(budgetRepository).findTopByUserOrderByYearDescMonthDesc(testUser);
        Mockito.verify(budgetRepository).save(any(Budget.class));
        Mockito.verifyNoMoreInteractions(budgetRepository);
    }

    @Test
    @DisplayName("Create next month budget in December creates January of the next year")
    public void testCreateNextMonthFromDecember() throws ChangeSetPersister.NotFoundException {
        Budget decemberBudget = new Budget();
        decemberBudget.setId(1L);
        decemberBudget.setDescription("Presupuesto Diciembre");
        decemberBudget.setMonth(12);
        decemberBudget.setYear(2025);
        decemberBudget.setEnabled(true);
        decemberBudget.setUser(testUser);
        decemberBudget.setRecurrentExpenses(Collections.emptyList());

        Mockito.when(budgetRepository.findTopByUserOrderByYearDescMonthDesc(testUser))
                .thenReturn(Optional.of(decemberBudget));
        Mockito.when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        BudgetDTO result = budgetService.createNextMonth();

        assertEquals(1, result.getMonth());
        assertEquals(2026, result.getYear());
        assertTrue(result.getEnabled());
        Mockito.verify(budgetRepository).findTopByUserOrderByYearDescMonthDesc(testUser);
        Mockito.verify(budgetRepository).save(any(Budget.class));
        Mockito.verifyNoMoreInteractions(budgetRepository);
        Mockito.verifyNoInteractions(recurrentExpenseRecordRepository);
    }

    @Test
    @DisplayName("Create next month budget throws NotFoundException when no previous budget exists")
    public void testCreateNextMonthThrowsWhenNoPreviousBudget() {
        Mockito.when(budgetRepository.findTopByUserOrderByYearDescMonthDesc(testUser))
                .thenReturn(Optional.empty());

        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> budgetService.createNextMonth());
        Mockito.verify(budgetRepository).findTopByUserOrderByYearDescMonthDesc(testUser);
        Mockito.verifyNoMoreInteractions(budgetRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // TOTALS CALCULATION
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Budget totals: all recurrent expenses cancelled shows full totals cancelled")
    public void testTotalsAllCancelled() throws ChangeSetPersister.NotFoundException {
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setDescription("Budget Marzo 2025");
        budget.setMonth(3);
        budget.setYear(2025);
        budget.setEnabled(true);
        budget.setUser(testUser);
        budget.setRecurrentExpenses(List.of(re1, re2));

        Mockito.when(budgetRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(budget));

        RecurrentExpenseRecord record1 = new RecurrentExpenseRecord();
        record1.setCancelled(true);
        record1.setRecurrentExpense(re1);

        RecurrentExpenseRecord record2 = new RecurrentExpenseRecord();
        record2.setCancelled(true);
        record2.setRecurrentExpense(re2);

        Mockito.when(recurrentExpenseRecordRepository.findByRecurrentExpenseAndMonthAndYear(re1, 3, 2025))
                .thenReturn(Optional.of(record1));
        Mockito.when(recurrentExpenseRecordRepository.findByRecurrentExpenseAndMonthAndYear(re2, 3, 2025))
                .thenReturn(Optional.of(record2));

        BudgetDTO result = budgetService.findById(1L);

        assertEquals(new BigDecimal("150000"), result.getTotalExpectedARS());
        assertEquals(new BigDecimal("120"), result.getTotalExpectedUSD());
        assertEquals(new BigDecimal("150000"), result.getTotalCancelledARS());
        assertEquals(new BigDecimal("120"), result.getTotalCancelledUSD());
        assertEquals(2, result.getCancelledCount());
        assertEquals(0, result.getPendingCount());
    }

    @Test
    @DisplayName("Budget totals: record exists but not cancelled does not add to cancelled totals")
    public void testTotalsRecordNotCancelled() throws ChangeSetPersister.NotFoundException {
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setDescription("Budget Abril 2025");
        budget.setMonth(4);
        budget.setYear(2025);
        budget.setEnabled(true);
        budget.setUser(testUser);
        budget.setRecurrentExpenses(List.of(re1));

        Mockito.when(budgetRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(budget));

        RecurrentExpenseRecord record1 = new RecurrentExpenseRecord();
        record1.setCancelled(false); // not cancelled
        record1.setRecurrentExpense(re1);

        Mockito.when(recurrentExpenseRecordRepository.findByRecurrentExpenseAndMonthAndYear(re1, 4, 2025))
                .thenReturn(Optional.of(record1));

        BudgetDTO result = budgetService.findById(1L);

        assertEquals(new BigDecimal("100000"), result.getTotalExpectedARS());
        assertEquals(BigDecimal.ZERO, result.getTotalCancelledARS());
        assertEquals(0, result.getCancelledCount());
        assertEquals(1, result.getPendingCount());
    }

}
