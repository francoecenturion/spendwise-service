package com.spendwise.unittest;

import com.spendwise.dto.CategoryDTO;
import com.spendwise.dto.PaymentMethodDTO;
import com.spendwise.dto.RecurrentExpenseDTO;
import com.spendwise.dto.RecurrentExpenseFilterDTO;
import com.spendwise.enums.PaymentMethodType;
import com.spendwise.model.Category;
import com.spendwise.model.Currency;
import com.spendwise.model.PaymentMethod;
import com.spendwise.model.RecurrentExpense;
import com.spendwise.model.auth.User;
import com.spendwise.repository.RecurrentExpenseRepository;
import com.spendwise.service.RecurrentExpenseService;
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
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurrentExpense Unit Tests")
public class RecurrentExpenseServiceTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private RecurrentExpenseRepository recurrentExpenseRepository;

    @InjectMocks
    private RecurrentExpenseService recurrentExpenseService;

    private User testUser;

    private static Category category;
    private static PaymentMethod paymentMethod;
    private static Currency currency;

    @BeforeAll
    public static void init() {
        category = new Category();
        category.setId(1L);
        category.setName("Servicios");
        category.setEnabled(true);

        paymentMethod = new PaymentMethod();
        paymentMethod.setId(1L);
        paymentMethod.setName("Transferencia");
        paymentMethod.setPaymentMethodType(PaymentMethodType.DEBIT_CARD);
        paymentMethod.setEnabled(true);

        currency = new Currency();
        currency.setId(1L);
        currency.setName("Peso Argentino");
        currency.setSymbol("$");
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
    @DisplayName("Create recurrent expense sets enabled to true and saves")
    public void testCreate() {
        // Arrange
        RecurrentExpenseDTO dto = new RecurrentExpenseDTO();
        dto.setDescription("Alquiler");
        dto.setAmountInPesos(new BigDecimal("100000"));
        dto.setAmountInDollars(new BigDecimal("80"));
        dto.setDayOfMonth(10);
        dto.setCategory(modelMapper.map(category, CategoryDTO.class));
        dto.setPaymentMethod(modelMapper.map(paymentMethod, PaymentMethodDTO.class));
        dto.setCurrency(currency);

        Mockito.when(recurrentExpenseRepository.save(any(RecurrentExpense.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        RecurrentExpenseDTO result = recurrentExpenseService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Alquiler", result.getDescription());
        assertEquals(10, result.getDayOfMonth());
        assertTrue(result.getEnabled());
        assertNull(result.getIcon());
        Mockito.verify(recurrentExpenseRepository).save(any(RecurrentExpense.class));
        Mockito.verifyNoMoreInteractions(recurrentExpenseRepository);
    }

    @Test
    @DisplayName("Create recurrent expense with icon stores icon URL")
    public void testCreateWithIcon() {
        // Arrange
        RecurrentExpenseDTO dto = new RecurrentExpenseDTO();
        dto.setDescription("Netflix");
        dto.setAmountInPesos(new BigDecimal("5000"));
        dto.setDayOfMonth(15);
        dto.setCategory(modelMapper.map(category, CategoryDTO.class));
        dto.setPaymentMethod(modelMapper.map(paymentMethod, PaymentMethodDTO.class));
        dto.setCurrency(currency);
        dto.setIcon("https://res.cloudinary.com/demo/image/upload/netflix.png");

        Mockito.when(recurrentExpenseRepository.save(any(RecurrentExpense.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        RecurrentExpenseDTO result = recurrentExpenseService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Netflix", result.getDescription());
        assertEquals("https://res.cloudinary.com/demo/image/upload/netflix.png", result.getIcon());
        assertTrue(result.getEnabled());
        Mockito.verify(recurrentExpenseRepository).save(any(RecurrentExpense.class));
        Mockito.verifyNoMoreInteractions(recurrentExpenseRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FIND BY ID
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Find recurrent expense by ID returns it when it exists")
    public void testFindById() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;
        RecurrentExpense entity = new RecurrentExpense();
        entity.setId(id);
        entity.setDescription("Alquiler");
        entity.setDayOfMonth(10);
        entity.setEnabled(true);
        entity.setUser(testUser);

        Mockito.when(recurrentExpenseRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));

        // Act
        RecurrentExpenseDTO result = recurrentExpenseService.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Alquiler", result.getDescription());
        Mockito.verify(recurrentExpenseRepository).findByIdAndUser(id, testUser);
        Mockito.verifyNoMoreInteractions(recurrentExpenseRepository);
    }

    @Test
    @DisplayName("Find recurrent expense by ID throws exception when it does not exist")
    public void testFindNonExistingById() {
        // Arrange
        Long id = 999L;
        Mockito.when(recurrentExpenseRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> recurrentExpenseService.findById(id));
        Mockito.verify(recurrentExpenseRepository).findByIdAndUser(id, testUser);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LIST
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("List all recurrent expenses returns complete list")
    public void testList() {
        // Arrange
        RecurrentExpense re1 = new RecurrentExpense();
        re1.setId(1L);
        re1.setDescription("Alquiler");
        re1.setDayOfMonth(10);
        re1.setEnabled(true);

        RecurrentExpense re2 = new RecurrentExpense();
        re2.setId(2L);
        re2.setDescription("Expensas");
        re2.setDayOfMonth(15);
        re2.setEnabled(true);

        Page<RecurrentExpense> page = new PageImpl<>(Arrays.asList(re1, re2));
        Pageable pageable = PageRequest.of(0, 20);
        RecurrentExpenseFilterDTO filters = new RecurrentExpenseFilterDTO();

        Mockito.when(recurrentExpenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<RecurrentExpenseDTO> result = recurrentExpenseService.list(filters, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals("Alquiler", result.getContent().get(0).getDescription());
        assertEquals("Expensas", result.getContent().get(1).getDescription());
    }

    @Test
    @DisplayName("List recurrent expenses with description filter")
    public void testListWithDescriptionFilter() {
        // Arrange
        RecurrentExpense re1 = new RecurrentExpense();
        re1.setId(1L);
        re1.setDescription("Alquiler");
        re1.setDayOfMonth(10);
        re1.setEnabled(true);

        Page<RecurrentExpense> page = new PageImpl<>(List.of(re1));
        Pageable pageable = PageRequest.of(0, 20);
        RecurrentExpenseFilterDTO filters = new RecurrentExpenseFilterDTO();
        filters.setDescription("Alqui");

        Mockito.when(recurrentExpenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<RecurrentExpenseDTO> result = recurrentExpenseService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getDescription().contains("Alqui"));
    }

    @Test
    @DisplayName("List recurrent expenses with enabled filter")
    public void testListWithEnabledFilter() {
        // Arrange
        RecurrentExpense re1 = new RecurrentExpense();
        re1.setId(1L);
        re1.setDescription("Alquiler");
        re1.setEnabled(true);

        Page<RecurrentExpense> page = new PageImpl<>(List.of(re1));
        Pageable pageable = PageRequest.of(0, 20);
        RecurrentExpenseFilterDTO filters = new RecurrentExpenseFilterDTO();
        filters.setEnabled(true);

        Mockito.when(recurrentExpenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<RecurrentExpenseDTO> result = recurrentExpenseService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List recurrent expenses with no results")
    public void testListWithNoResults() {
        // Arrange
        Page<RecurrentExpense> page = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 20);
        RecurrentExpenseFilterDTO filters = new RecurrentExpenseFilterDTO();

        Mockito.when(recurrentExpenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<RecurrentExpenseDTO> result = recurrentExpenseService.list(filters, pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Update recurrent expense modifies existing data without changing enabled")
    public void testUpdate() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;
        RecurrentExpense entity = new RecurrentExpense();
        entity.setId(id);
        entity.setDescription("Alquiler");
        entity.setDayOfMonth(10);
        entity.setEnabled(true);
        entity.setUser(testUser);

        RecurrentExpenseDTO updateDTO = new RecurrentExpenseDTO();
        updateDTO.setDescription("Alquiler nuevo");
        updateDTO.setDayOfMonth(12);
        updateDTO.setAmountInPesos(new BigDecimal("120000"));
        updateDTO.setAmountInDollars(new BigDecimal("90"));
        updateDTO.setIcon("https://res.cloudinary.com/demo/image/upload/alquiler.png");

        Mockito.when(recurrentExpenseRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));
        Mockito.when(recurrentExpenseRepository.save(any(RecurrentExpense.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RecurrentExpenseDTO result = recurrentExpenseService.update(id, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Alquiler nuevo", result.getDescription());
        assertEquals(12, result.getDayOfMonth());
        assertEquals("https://res.cloudinary.com/demo/image/upload/alquiler.png", result.getIcon());
        Mockito.verify(recurrentExpenseRepository).findByIdAndUser(id, testUser);
        Mockito.verify(recurrentExpenseRepository).save(entity);
        Mockito.verifyNoMoreInteractions(recurrentExpenseRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Delete recurrent expense removes it from the database")
    public void testDelete() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;
        RecurrentExpense entity = new RecurrentExpense();
        entity.setId(id);
        entity.setDescription("Alquiler");
        entity.setEnabled(true);
        entity.setUser(testUser);

        Mockito.when(recurrentExpenseRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));

        // Act
        RecurrentExpenseDTO result = recurrentExpenseService.delete(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        Mockito.verify(recurrentExpenseRepository).findByIdAndUser(id, testUser);
        Mockito.verify(recurrentExpenseRepository).delete(entity);
        Mockito.verifyNoMoreInteractions(recurrentExpenseRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ENABLE / DISABLE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Enable recurrent expense sets enabled to true")
    public void testEnable() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;
        RecurrentExpense entity = new RecurrentExpense();
        entity.setId(id);
        entity.setDescription("Alquiler");
        entity.setEnabled(false);
        entity.setUser(testUser);

        Mockito.when(recurrentExpenseRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));
        Mockito.when(recurrentExpenseRepository.save(any(RecurrentExpense.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RecurrentExpenseDTO result = recurrentExpenseService.enable(id);

        // Assert
        assertTrue(result.getEnabled());
        Mockito.verify(recurrentExpenseRepository).findByIdAndUser(id, testUser);
        Mockito.verify(recurrentExpenseRepository).save(entity);
        Mockito.verifyNoMoreInteractions(recurrentExpenseRepository);
    }

    @Test
    @DisplayName("Disable recurrent expense sets enabled to false")
    public void testDisable() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;
        RecurrentExpense entity = new RecurrentExpense();
        entity.setId(id);
        entity.setDescription("Alquiler");
        entity.setEnabled(true);
        entity.setUser(testUser);

        Mockito.when(recurrentExpenseRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));
        Mockito.when(recurrentExpenseRepository.save(any(RecurrentExpense.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RecurrentExpenseDTO result = recurrentExpenseService.disable(id);

        // Assert
        assertFalse(result.getEnabled());
        Mockito.verify(recurrentExpenseRepository).findByIdAndUser(id, testUser);
        Mockito.verify(recurrentExpenseRepository).save(entity);
        Mockito.verifyNoMoreInteractions(recurrentExpenseRepository);
    }

}
