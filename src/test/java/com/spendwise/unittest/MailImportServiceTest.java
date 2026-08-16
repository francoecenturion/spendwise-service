package com.spendwise.unittest;

import com.spendwise.dto.ExpenseDTO;
import com.spendwise.dto.MailImportConfirmDTO;
import com.spendwise.dto.MailImportDTO;
import com.spendwise.dto.MerchantBindingDTO;
import com.spendwise.enums.MailImportStatus;
import com.spendwise.model.Category;
import com.spendwise.model.MailImport;
import com.spendwise.model.MerchantBinding;
import com.spendwise.model.PaymentMethod;
import com.spendwise.model.auth.User;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.MailImportRepository;
import com.spendwise.repository.MerchantBindingRepository;
import com.spendwise.repository.PaymentMethodRepository;
import com.spendwise.service.MailImportService;
import com.spendwise.service.interfaces.IDebtService;
import com.spendwise.service.interfaces.IExpenseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("MailImportService Unit Tests")
public class MailImportServiceTest {

    @Mock private MailImportRepository mailImportRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private PaymentMethodRepository paymentMethodRepository;
    @Mock private MerchantBindingRepository merchantBindingRepository;
    @Mock private IExpenseService expenseService;
    @Mock private IDebtService debtService;

    @InjectMocks
    private MailImportService mailImportService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setEnabled(true);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MailImport buildPendingImport(Long id, boolean isDebt) {
        MailImport m = new MailImport();
        m.setId(id);
        m.setUser(testUser);
        m.setImapMessageId("msg-" + id);
        m.setStatus(MailImportStatus.PENDING);
        m.setParsedMerchant("Comercio Test");
        m.setParsedAmount(BigDecimal.valueOf(1000));
        m.setParsedDate(LocalDate.of(2026, 1, 15));
        m.setParsedIsDebt(isDebt);
        m.setSenderEntity("MercadoPago");
        return m;
    }

    private Category buildCategory(Long id) {
        Category c = new Category();
        c.setId(id);
        c.setName("Comida");
        return c;
    }

    private PaymentMethod buildPaymentMethod(Long id) {
        PaymentMethod pm = new PaymentMethod();
        pm.setId(id);
        pm.setName("Mercado Pago");
        return pm;
    }

    // ── ignore ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ignore sets status to IGNORED and saves")
    public void testIgnore() throws ChangeSetPersister.NotFoundException {
        MailImport mailImport = buildPendingImport(1L, false);
        Mockito.when(mailImportRepository.findById(1L)).thenReturn(Optional.of(mailImport));
        Mockito.when(mailImportRepository.save(mailImport)).thenReturn(mailImport);

        MailImportDTO result = mailImportService.ignore(1L);

        assertEquals(MailImportStatus.IGNORED, result.getStatus());
        Mockito.verify(mailImportRepository).save(mailImport);
    }

    @Test
    @DisplayName("ignore throws NotFoundException when import does not belong to current user")
    public void testIgnore_notFound() {
        User otherUser = new User();
        otherUser.setId(99L);
        MailImport mailImport = buildPendingImport(1L, false);
        mailImport.setUser(otherUser);
        Mockito.when(mailImportRepository.findById(1L)).thenReturn(Optional.of(mailImport));

        assertThrows(ChangeSetPersister.NotFoundException.class, () -> mailImportService.ignore(1L));
        Mockito.verify(mailImportRepository, Mockito.never()).save(any());
    }

    // ── confirm: expense path ─────────────────────────────────────────────────

    @Test
    @DisplayName("confirm creates Expense when parsedIsDebt=false")
    public void testConfirm_expense() throws ChangeSetPersister.NotFoundException {
        MailImport mailImport = buildPendingImport(1L, false);
        Category category = buildCategory(10L);
        PaymentMethod pm = buildPaymentMethod(20L);

        MailImportConfirmDTO dto = new MailImportConfirmDTO();
        dto.setCategoryId(10L);
        dto.setPaymentMethodId(20L);
        dto.setDescription("Almuerzo");

        Mockito.when(mailImportRepository.findById(1L)).thenReturn(Optional.of(mailImport));
        Mockito.when(categoryRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(category));
        Mockito.when(paymentMethodRepository.findByIdAndUser(20L, testUser)).thenReturn(Optional.of(pm));
        ExpenseDTO createdExpense = new ExpenseDTO();
        createdExpense.setId(100L);
        Mockito.when(expenseService.create(any())).thenReturn(createdExpense);
        Mockito.when(merchantBindingRepository.findByUserAndMerchantNameIgnoreCase(testUser, "Comercio Test"))
                .thenReturn(Optional.empty());
        Mockito.when(mailImportRepository.save(mailImport)).thenReturn(mailImport);

        MailImportDTO result = mailImportService.confirm(1L, dto);

        assertEquals(MailImportStatus.CONFIRMED, result.getStatus());
        Mockito.verify(expenseService).create(any());
        Mockito.verify(debtService, Mockito.never()).create(any());
        Mockito.verify(merchantBindingRepository).save(any(MerchantBinding.class));
    }

    @Test
    @DisplayName("confirm creates Expense with description from DTO when provided")
    public void testConfirm_expense_usesDescriptionFromDTO() throws ChangeSetPersister.NotFoundException {
        MailImport mailImport = buildPendingImport(1L, false);
        Category category = buildCategory(10L);

        MailImportConfirmDTO dto = new MailImportConfirmDTO();
        dto.setCategoryId(10L);
        dto.setDescription("Mi descripción personalizada");

        Mockito.when(mailImportRepository.findById(1L)).thenReturn(Optional.of(mailImport));
        Mockito.when(categoryRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(category));
        ExpenseDTO createdExpense = new ExpenseDTO();
        createdExpense.setId(5L);
        Mockito.when(expenseService.create(any())).thenAnswer(inv -> {
            ExpenseDTO e = inv.getArgument(0);
            assertEquals("Mi descripción personalizada", e.getDescription());
            return createdExpense;
        });
        Mockito.when(merchantBindingRepository.findByUserAndMerchantNameIgnoreCase(any(), any()))
                .thenReturn(Optional.empty());
        Mockito.when(mailImportRepository.save(any())).thenReturn(mailImport);

        mailImportService.confirm(1L, dto);

        Mockito.verify(expenseService).create(any());
    }

    @Test
    @DisplayName("confirm falls back to parsedMerchant as description when DTO description is null")
    public void testConfirm_expense_fallbackDescription() throws ChangeSetPersister.NotFoundException {
        MailImport mailImport = buildPendingImport(1L, false);
        Category category = buildCategory(10L);

        MailImportConfirmDTO dto = new MailImportConfirmDTO();
        dto.setCategoryId(10L);
        // description = null → should fall back to parsedMerchant

        Mockito.when(mailImportRepository.findById(1L)).thenReturn(Optional.of(mailImport));
        Mockito.when(categoryRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(category));
        ExpenseDTO createdExpense = new ExpenseDTO();
        createdExpense.setId(5L);
        Mockito.when(expenseService.create(any())).thenAnswer(inv -> {
            ExpenseDTO e = inv.getArgument(0);
            assertEquals("Comercio Test", e.getDescription());
            return createdExpense;
        });
        Mockito.when(merchantBindingRepository.findByUserAndMerchantNameIgnoreCase(any(), any()))
                .thenReturn(Optional.empty());
        Mockito.when(mailImportRepository.save(any())).thenReturn(mailImport);

        mailImportService.confirm(1L, dto);

        Mockito.verify(expenseService).create(any());
    }

    // ── confirm: debt path ────────────────────────────────────────────────────

    @Test
    @DisplayName("confirm creates Debt when parsedIsDebt=true")
    public void testConfirm_debt() throws ChangeSetPersister.NotFoundException {
        MailImport mailImport = buildPendingImport(1L, true);
        PaymentMethod pm = buildPaymentMethod(20L);

        MailImportConfirmDTO dto = new MailImportConfirmDTO();
        dto.setPaymentMethodId(20L);

        Mockito.when(mailImportRepository.findById(1L)).thenReturn(Optional.of(mailImport));
        Mockito.when(paymentMethodRepository.findByIdAndUser(20L, testUser)).thenReturn(Optional.of(pm));
        Mockito.when(merchantBindingRepository.findByUserAndMerchantNameIgnoreCase(any(), any()))
                .thenReturn(Optional.empty());
        Mockito.when(mailImportRepository.save(mailImport)).thenReturn(mailImport);

        MailImportDTO result = mailImportService.confirm(1L, dto);

        assertEquals(MailImportStatus.CONFIRMED, result.getStatus());
        Mockito.verify(debtService).create(any());
        Mockito.verify(expenseService, Mockito.never()).create(any());
    }

    @Test
    @DisplayName("confirm throws IllegalStateException when import is not PENDING")
    public void testConfirm_notPending() {
        MailImport mailImport = buildPendingImport(1L, false);
        mailImport.setStatus(MailImportStatus.CONFIRMED);

        Mockito.when(mailImportRepository.findById(1L)).thenReturn(Optional.of(mailImport));

        MailImportConfirmDTO dto = new MailImportConfirmDTO();
        dto.setCategoryId(10L);

        assertThrows(IllegalStateException.class, () -> mailImportService.confirm(1L, dto));
        Mockito.verify(expenseService, Mockito.never()).create(any());
    }

    @Test
    @DisplayName("confirm throws NotFoundException when import does not belong to current user")
    public void testConfirm_notFound() {
        User otherUser = new User();
        otherUser.setId(99L);
        MailImport mailImport = buildPendingImport(1L, false);
        mailImport.setUser(otherUser);

        Mockito.when(mailImportRepository.findById(1L)).thenReturn(Optional.of(mailImport));

        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> mailImportService.confirm(1L, new MailImportConfirmDTO()));
    }

    // ── getPendingCount ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getPendingCount delegates to repository count query")
    public void testGetPendingCount() {
        Mockito.when(mailImportRepository.countByUserAndStatus(testUser, MailImportStatus.PENDING))
                .thenReturn(5L);

        long count = mailImportService.getPendingCount();

        assertEquals(5L, count);
        Mockito.verify(mailImportRepository).countByUserAndStatus(testUser, MailImportStatus.PENDING);
    }

    // ── lookupBinding ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("lookupBinding returns DTO when binding exists")
    public void testLookupBinding_found() {
        Category category = buildCategory(10L);
        PaymentMethod pm = buildPaymentMethod(20L);
        MerchantBinding binding = new MerchantBinding();
        binding.setMerchantName("McDonald's");
        binding.setCategory(category);
        binding.setPaymentMethod(pm);
        binding.setDescription("Almuerzo");

        Mockito.when(merchantBindingRepository.findByUserAndMerchantNameIgnoreCase(testUser, "McDonald's"))
                .thenReturn(Optional.of(binding));

        MerchantBindingDTO result = mailImportService.lookupBinding("McDonald's");

        assertNotNull(result);
        assertEquals(10L, result.getCategoryId());
        assertEquals(20L, result.getPaymentMethodId());
        assertEquals("Almuerzo", result.getDescription());
    }

    @Test
    @DisplayName("lookupBinding returns null when no binding exists")
    public void testLookupBinding_notFound() {
        Mockito.when(merchantBindingRepository.findByUserAndMerchantNameIgnoreCase(testUser, "Unknown"))
                .thenReturn(Optional.empty());

        MerchantBindingDTO result = mailImportService.lookupBinding("Unknown");

        assertNull(result);
    }

    // ── saveBinding: upsert ───────────────────────────────────────────────────

    @Test
    @DisplayName("confirm updates existing MerchantBinding when one already exists")
    public void testConfirm_updatesExistingBinding() throws ChangeSetPersister.NotFoundException {
        MailImport mailImport = buildPendingImport(1L, false);
        Category category = buildCategory(10L);

        MailImportConfirmDTO dto = new MailImportConfirmDTO();
        dto.setCategoryId(10L);

        MerchantBinding existingBinding = new MerchantBinding();
        existingBinding.setUser(testUser);
        existingBinding.setMerchantName("Comercio Test");

        Mockito.when(mailImportRepository.findById(1L)).thenReturn(Optional.of(mailImport));
        Mockito.when(categoryRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(category));
        Mockito.when(expenseService.create(any())).thenReturn(new ExpenseDTO());
        Mockito.when(merchantBindingRepository.findByUserAndMerchantNameIgnoreCase(testUser, "Comercio Test"))
                .thenReturn(Optional.of(existingBinding));
        Mockito.when(mailImportRepository.save(any())).thenReturn(mailImport);

        mailImportService.confirm(1L, dto);

        // Should save the existing binding (not create a new one)
        Mockito.verify(merchantBindingRepository).save(existingBinding);
    }
}
