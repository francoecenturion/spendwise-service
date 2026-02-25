package com.spendwise.unittest;

import com.spendwise.client.dolarApi.DolarApiClient;
import com.spendwise.client.dolarApi.DolarApiDTO;
import com.spendwise.client.dolarApiHistorical.DolarApiHistoricalClient;
import com.spendwise.client.dolarApiHistorical.DolarApiHistoricalDTO;
import com.spendwise.dto.CategoryDTO;
import com.spendwise.dto.ExpenseDTO;
import com.spendwise.dto.ExpenseFilterDTO;
import com.spendwise.dto.PaymentMethodDTO;
import com.spendwise.enums.PaymentMethodType;
import com.spendwise.model.Category;
import com.spendwise.model.Currency;
import com.spendwise.model.Expense;
import com.spendwise.model.PaymentMethod;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.service.ExpenseService;
import org.junit.jupiter.api.BeforeAll;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("Expense Unit Tests")
public class ExpenseServiceTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private DolarApiClient dolarApiClient;

    @Mock
    private DolarApiHistoricalClient dolarApiHistoricalClient;

    @InjectMocks
    private ExpenseService expenseService;

    private static PaymentMethod paymentMethod;
    private static Category category;
    private static Currency currencyARS;
    private static Currency currencyUSD;

    @BeforeAll
    public static void init() {
        category = new Category();
        category.setId(1L);
        category.setName("Groceries");
        category.setEnabled(true);

        paymentMethod = new PaymentMethod();
        paymentMethod.setId(1L);
        paymentMethod.setName("Mercado Pago");
        paymentMethod.setPaymentMethodType(PaymentMethodType.DEBIT_CARD);
        paymentMethod.setEnabled(true);

        currencyARS = new Currency();
        currencyARS.setId(1L);
        currencyARS.setName("Peso Argentino");
        currencyARS.setSymbol("$");

        currencyUSD = new Currency();
        currencyUSD.setId(2L);
        currencyUSD.setName("Dolar Estadounidense");
        currencyUSD.setSymbol("USD");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Create expense in ARS with today's date uses DolarApiClient to calculate dollars")
    public void testCreateWithArsCurrencyToday() {
        // Arrange
        LocalDate today = LocalDate.now();
        BigDecimal inputAmount = new BigDecimal("3000");
        BigDecimal sellingPrice = new BigDecimal("1500");
        BigDecimal expectedDollars = inputAmount.divide(sellingPrice, 4, RoundingMode.HALF_EVEN);

        ExpenseDTO dto = new ExpenseDTO();
        dto.setDescription("Greengrocers");
        dto.setInputAmount(inputAmount);
        dto.setDate(today);
        dto.setCategory(modelMapper.map(category, CategoryDTO.class));
        dto.setPaymentMethod(modelMapper.map(paymentMethod, PaymentMethodDTO.class));
        dto.setCurrency(currencyARS);

        DolarApiDTO dolarApiDTO = new DolarApiDTO();
        dolarApiDTO.setSellingPrice(sellingPrice);

        Mockito.when(dolarApiClient.getRate("oficial")).thenReturn(dolarApiDTO);
        Mockito.when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ExpenseDTO result = expenseService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Greengrocers", result.getDescription());
        assertEquals(inputAmount, result.getAmountInPesos());
        assertEquals(expectedDollars, result.getAmountInDollars());
        Mockito.verify(dolarApiClient).getRate("oficial");
        Mockito.verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("Create expense in ARS with a past date uses DolarApiHistoricalClient to calculate dollars")
    public void testCreateWithArsCurrencyPastDate() {
        // Arrange
        LocalDate pastDate = LocalDate.of(2024, 6, 15);
        BigDecimal inputAmount = new BigDecimal("5000");
        BigDecimal sellingPrice = new BigDecimal("900");
        BigDecimal expectedDollars = inputAmount.divide(sellingPrice, 4, RoundingMode.HALF_EVEN);

        ExpenseDTO dto = new ExpenseDTO();
        dto.setDescription("Supermercado junio");
        dto.setInputAmount(inputAmount);
        dto.setDate(pastDate);
        dto.setCategory(modelMapper.map(category, CategoryDTO.class));
        dto.setPaymentMethod(modelMapper.map(paymentMethod, PaymentMethodDTO.class));
        dto.setCurrency(currencyARS);

        DolarApiHistoricalDTO historicalDTO = new DolarApiHistoricalDTO();
        historicalDTO.setSellingPrice(sellingPrice);

        Mockito.when(dolarApiHistoricalClient.getRate("oficial", pastDate.toString())).thenReturn(historicalDTO);
        Mockito.when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ExpenseDTO result = expenseService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Supermercado junio", result.getDescription());
        assertEquals(inputAmount, result.getAmountInPesos());
        assertEquals(expectedDollars, result.getAmountInDollars());
        Mockito.verify(dolarApiHistoricalClient).getRate("oficial", pastDate.toString());
        Mockito.verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("Create expense in USD with today's date uses DolarApiClient to calculate pesos")
    public void testCreateWithUsdCurrencyToday() {
        // Arrange
        LocalDate today = LocalDate.now();
        BigDecimal inputAmount = new BigDecimal("100");
        BigDecimal sellingPrice = new BigDecimal("1500");
        BigDecimal expectedPesos = inputAmount.multiply(sellingPrice);

        ExpenseDTO dto = new ExpenseDTO();
        dto.setDescription("Netflix USD");
        dto.setInputAmount(inputAmount);
        dto.setDate(today);
        dto.setCategory(modelMapper.map(category, CategoryDTO.class));
        dto.setPaymentMethod(modelMapper.map(paymentMethod, PaymentMethodDTO.class));
        dto.setCurrency(currencyUSD);

        DolarApiDTO dolarApiDTO = new DolarApiDTO();
        dolarApiDTO.setSellingPrice(sellingPrice);

        Mockito.when(dolarApiClient.getRate("oficial")).thenReturn(dolarApiDTO);
        Mockito.when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ExpenseDTO result = expenseService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Netflix USD", result.getDescription());
        assertEquals(inputAmount, result.getAmountInDollars());
        assertEquals(expectedPesos, result.getAmountInPesos());
        Mockito.verify(dolarApiClient).getRate("oficial");
        Mockito.verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("Create expense in USD with a past date uses DolarApiHistoricalClient to calculate pesos")
    public void testCreateWithUsdCurrencyPastDate() {
        // Arrange
        LocalDate pastDate = LocalDate.of(2024, 3, 10);
        BigDecimal inputAmount = new BigDecimal("50");
        BigDecimal sellingPrice = new BigDecimal("850");
        BigDecimal expectedPesos = inputAmount.multiply(sellingPrice);

        ExpenseDTO dto = new ExpenseDTO();
        dto.setDescription("Spotify USD marzo");
        dto.setInputAmount(inputAmount);
        dto.setDate(pastDate);
        dto.setCategory(modelMapper.map(category, CategoryDTO.class));
        dto.setPaymentMethod(modelMapper.map(paymentMethod, PaymentMethodDTO.class));
        dto.setCurrency(currencyUSD);

        DolarApiHistoricalDTO historicalDTO = new DolarApiHistoricalDTO();
        historicalDTO.setSellingPrice(sellingPrice);

        Mockito.when(dolarApiHistoricalClient.getRate("oficial", pastDate.toString())).thenReturn(historicalDTO);
        Mockito.when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ExpenseDTO result = expenseService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Spotify USD marzo", result.getDescription());
        assertEquals(inputAmount, result.getAmountInDollars());
        assertEquals(expectedPesos, result.getAmountInPesos());
        Mockito.verify(dolarApiHistoricalClient).getRate("oficial", pastDate.toString());
        Mockito.verify(expenseRepository).save(any(Expense.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FIND BY ID
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Find expense by ID returns the expense when it exists")
    public void testFindById() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;
        Expense expense = new Expense();
        expense.setId(id);
        expense.setDescription("Supermarket");
        expense.setAmountInPesos(BigDecimal.valueOf(5000));
        expense.setAmountInDollars(BigDecimal.valueOf(3));
        expense.setDate(LocalDate.now());
        expense.setCategory(category);
        expense.setPaymentMethod(paymentMethod);
        expense.setCurrency(currencyARS);

        // Act
        Mockito.when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));
        ExpenseDTO result = expenseService.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Supermarket", result.getDescription());
        Mockito.verify(expenseRepository).findById(id);
    }

    @Test
    @DisplayName("Find expense by ID throws exception when expense does not exist")
    public void testFindNonExistingById() {
        // Arrange
        Long id = 999L;

        // Act & Assert
        Mockito.when(expenseRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> expenseService.findById(id));
        Mockito.verify(expenseRepository).findById(id);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LIST
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("List all expenses returns complete list")
    public void testList() {
        // Arrange
        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setDescription("Supermarket");
        expense1.setAmountInPesos(BigDecimal.valueOf(5000));
        expense1.setAmountInDollars(BigDecimal.valueOf(3));
        expense1.setDate(LocalDate.now());
        expense1.setCategory(category);
        expense1.setPaymentMethod(paymentMethod);
        expense1.setCurrency(currencyARS);

        Expense expense2 = new Expense();
        expense2.setId(2L);
        expense2.setDescription("Greengrocers");
        expense2.setAmountInPesos(BigDecimal.valueOf(2500));
        expense2.setAmountInDollars(BigDecimal.valueOf(1.5));
        expense2.setDate(LocalDate.now());
        expense2.setCategory(category);
        expense2.setPaymentMethod(paymentMethod);
        expense2.setCurrency(currencyARS);

        List<Expense> expenses = Arrays.asList(expense1, expense2);
        Page<Expense> expensePage = new PageImpl<>(expenses);

        Pageable pageable = PageRequest.of(0, 20);
        ExpenseFilterDTO filters = new ExpenseFilterDTO();

        // Act
        Mockito.when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expensePage);

        Page<ExpenseDTO> result = expenseService.list(filters, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Supermarket", result.getContent().get(0).getDescription());
        assertEquals("Greengrocers", result.getContent().get(1).getDescription());
    }

    @Test
    @DisplayName("List expenses with description filter")
    public void testListWithDescriptionFilter() {
        // Arrange
        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setDescription("Supermarket purchase");
        expense1.setAmountInPesos(BigDecimal.valueOf(5000));
        expense1.setAmountInDollars(BigDecimal.valueOf(3));
        expense1.setDate(LocalDate.now());
        expense1.setCategory(category);
        expense1.setPaymentMethod(paymentMethod);
        expense1.setCurrency(currencyARS);

        List<Expense> expenses = Arrays.asList(expense1);
        Page<Expense> expensePage = new PageImpl<>(expenses);

        Pageable pageable = PageRequest.of(0, 20);
        ExpenseFilterDTO filters = new ExpenseFilterDTO();
        filters.setDescription("Supermarket");

        // Act
        Mockito.when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expensePage);

        Page<ExpenseDTO> result = expenseService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getDescription().contains("Supermarket"));
    }

    @Test
    @DisplayName("List expenses with amount in pesos range filter")
    public void testListWithAmountInPesosRangeFilter() {
        // Arrange
        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setDescription("Mid-range purchase");
        expense1.setAmountInPesos(BigDecimal.valueOf(3000));
        expense1.setAmountInDollars(BigDecimal.valueOf(2));
        expense1.setDate(LocalDate.now());
        expense1.setCategory(category);
        expense1.setPaymentMethod(paymentMethod);
        expense1.setCurrency(currencyARS);

        List<Expense> expenses = Arrays.asList(expense1);
        Page<Expense> expensePage = new PageImpl<>(expenses);

        Pageable pageable = PageRequest.of(0, 20);
        ExpenseFilterDTO filters = new ExpenseFilterDTO();
        filters.setMinAmountInPesos(BigDecimal.valueOf(2000));
        filters.setMaxAmountInPesos(BigDecimal.valueOf(4000));

        // Act
        Mockito.when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expensePage);

        Page<ExpenseDTO> result = expenseService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getAmountInPesos().compareTo(BigDecimal.valueOf(2000)) >= 0);
        assertTrue(result.getContent().get(0).getAmountInPesos().compareTo(BigDecimal.valueOf(4000)) <= 0);
    }

    @Test
    @DisplayName("List expenses with amount in dollars range filter")
    public void testListWithAmountInDollarsRangeFilter() {
        // Arrange
        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setDescription("Dollar purchase");
        expense1.setAmountInPesos(BigDecimal.valueOf(3000));
        expense1.setAmountInDollars(BigDecimal.valueOf(2));
        expense1.setDate(LocalDate.now());
        expense1.setCategory(category);
        expense1.setPaymentMethod(paymentMethod);
        expense1.setCurrency(currencyUSD);

        List<Expense> expenses = Arrays.asList(expense1);
        Page<Expense> expensePage = new PageImpl<>(expenses);

        Pageable pageable = PageRequest.of(0, 20);
        ExpenseFilterDTO filters = new ExpenseFilterDTO();
        filters.setMinAmountInDollars(BigDecimal.valueOf(1));
        filters.setMaxAmountInDollars(BigDecimal.valueOf(3));

        // Act
        Mockito.when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expensePage);

        Page<ExpenseDTO> result = expenseService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getAmountInDollars().compareTo(BigDecimal.valueOf(1)) >= 0);
        assertTrue(result.getContent().get(0).getAmountInDollars().compareTo(BigDecimal.valueOf(3)) <= 0);
    }

    @Test
    @DisplayName("List expenses with date range filter")
    public void testListWithDateRangeFilter() {
        // Arrange
        LocalDate today = LocalDate.now();

        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setDescription("Recent purchase");
        expense1.setAmountInPesos(BigDecimal.valueOf(3000));
        expense1.setAmountInDollars(BigDecimal.valueOf(2));
        expense1.setDate(today);
        expense1.setCategory(category);
        expense1.setPaymentMethod(paymentMethod);
        expense1.setCurrency(currencyARS);

        List<Expense> expenses = Arrays.asList(expense1);
        Page<Expense> expensePage = new PageImpl<>(expenses);

        Pageable pageable = PageRequest.of(0, 20);
        ExpenseFilterDTO filters = new ExpenseFilterDTO();
        filters.setStartDate(today.minusDays(7));
        filters.setEndDate(today.plusDays(1));

        // Act
        Mockito.when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expensePage);

        Page<ExpenseDTO> result = expenseService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertNotNull(result.getContent().get(0).getDate());
    }

    @Test
    @DisplayName("List expenses with category ID filter")
    public void testListWithCategoryIdFilter() {
        // Arrange
        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setDescription("Grocery purchase");
        expense1.setAmountInPesos(BigDecimal.valueOf(3000));
        expense1.setAmountInDollars(BigDecimal.valueOf(2));
        expense1.setDate(LocalDate.now());
        expense1.setCategory(category);
        expense1.setPaymentMethod(paymentMethod);
        expense1.setCurrency(currencyARS);

        List<Expense> expenses = Arrays.asList(expense1);
        Page<Expense> expensePage = new PageImpl<>(expenses);

        Pageable pageable = PageRequest.of(0, 20);
        ExpenseFilterDTO filters = new ExpenseFilterDTO();
        filters.setCategoryId(1L);

        // Act
        Mockito.when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expensePage);

        Page<ExpenseDTO> result = expenseService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getCategory().getId());
    }

    @Test
    @DisplayName("List expenses with payment method ID filter")
    public void testListWithPaymentMethodIdFilter() {
        // Arrange
        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setDescription("Card purchase");
        expense1.setAmountInPesos(BigDecimal.valueOf(3000));
        expense1.setAmountInDollars(BigDecimal.valueOf(2));
        expense1.setDate(LocalDate.now());
        expense1.setCategory(category);
        expense1.setPaymentMethod(paymentMethod);
        expense1.setCurrency(currencyARS);

        List<Expense> expenses = Arrays.asList(expense1);
        Page<Expense> expensePage = new PageImpl<>(expenses);

        Pageable pageable = PageRequest.of(0, 20);
        ExpenseFilterDTO filters = new ExpenseFilterDTO();
        filters.setPaymentMethodId(1L);

        // Act
        Mockito.when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expensePage);

        Page<ExpenseDTO> result = expenseService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getPaymentMethod().getId());
    }

    @Test
    @DisplayName("List expenses with multiple filters")
    public void testListWithMultipleFilters() {
        // Arrange
        LocalDate today = LocalDate.now();

        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setDescription("Supermarket groceries");
        expense1.setAmountInPesos(BigDecimal.valueOf(3000));
        expense1.setAmountInDollars(BigDecimal.valueOf(2));
        expense1.setDate(today);
        expense1.setCategory(category);
        expense1.setPaymentMethod(paymentMethod);
        expense1.setCurrency(currencyARS);

        List<Expense> expenses = Arrays.asList(expense1);
        Page<Expense> expensePage = new PageImpl<>(expenses);

        Pageable pageable = PageRequest.of(0, 20);
        ExpenseFilterDTO filters = new ExpenseFilterDTO();
        filters.setDescription("Supermarket");
        filters.setMinAmountInPesos(BigDecimal.valueOf(2000));
        filters.setMaxAmountInPesos(BigDecimal.valueOf(5000));
        filters.setCategoryId(1L);
        filters.setPaymentMethodId(1L);
        filters.setStartDate(today.minusDays(1));
        filters.setEndDate(today.plusDays(1));

        // Act
        Mockito.when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expensePage);

        Page<ExpenseDTO> result = expenseService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Supermarket groceries", result.getContent().get(0).getDescription());
        assertEquals(1L, result.getContent().get(0).getCategory().getId());
        assertEquals(1L, result.getContent().get(0).getPaymentMethod().getId());
    }

    @Test
    @DisplayName("List expenses with no results")
    public void testListWithNoResults() {
        // Arrange
        List<Expense> expenses = Collections.emptyList();
        Page<Expense> expensePage = new PageImpl<>(expenses);

        Pageable pageable = PageRequest.of(0, 20);
        ExpenseFilterDTO filters = new ExpenseFilterDTO();
        filters.setDescription("NonExistentExpense");

        // Act
        Mockito.when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expensePage);

        Page<ExpenseDTO> result = expenseService.list(filters, pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("List expenses with pagination")
    public void testListWithPagination() {
        // Arrange
        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setDescription("Expense 1");
        expense1.setAmountInPesos(BigDecimal.valueOf(1000));
        expense1.setAmountInDollars(BigDecimal.valueOf(0.67));
        expense1.setDate(LocalDate.now());
        expense1.setCategory(category);
        expense1.setPaymentMethod(paymentMethod);
        expense1.setCurrency(currencyARS);

        Expense expense2 = new Expense();
        expense2.setId(2L);
        expense2.setDescription("Expense 2");
        expense2.setAmountInPesos(BigDecimal.valueOf(2000));
        expense2.setAmountInDollars(BigDecimal.valueOf(1.33));
        expense2.setDate(LocalDate.now());
        expense2.setCategory(category);
        expense2.setPaymentMethod(paymentMethod);
        expense2.setCurrency(currencyARS);

        List<Expense> expenses = Arrays.asList(expense1, expense2);
        Page<Expense> expensePage = new PageImpl<>(expenses, PageRequest.of(0, 10), 25);

        Pageable pageable = PageRequest.of(0, 10);
        ExpenseFilterDTO filters = new ExpenseFilterDTO();

        // Act
        Mockito.when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expensePage);

        Page<ExpenseDTO> result = expenseService.list(filters, pageable);

        // Assert
        assertEquals(25, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalPages());
        assertEquals(0, result.getNumber());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Update expense in ARS recalculates amountInDollars via DolarApiClient")
    public void testUpdateWithArsCurrency() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;
        LocalDate today = LocalDate.now();
        BigDecimal newAmount = new BigDecimal("4000");
        BigDecimal sellingPrice = new BigDecimal("1500");
        BigDecimal expectedDollars = newAmount.divide(sellingPrice, 4, RoundingMode.HALF_EVEN);

        Expense existingExpense = new Expense();
        existingExpense.setId(id);
        existingExpense.setDescription("Old description");
        existingExpense.setAmountInPesos(BigDecimal.valueOf(1000));
        existingExpense.setDate(today);
        existingExpense.setCategory(category);
        existingExpense.setPaymentMethod(paymentMethod);
        existingExpense.setCurrency(currencyARS);

        ExpenseDTO updateDTO = new ExpenseDTO();
        updateDTO.setDescription("Updated description");
        updateDTO.setInputAmount(newAmount);
        updateDTO.setDate(today);
        updateDTO.setCategory(modelMapper.map(category, CategoryDTO.class));
        updateDTO.setPaymentMethod(modelMapper.map(paymentMethod, PaymentMethodDTO.class));
        updateDTO.setCurrency(currencyARS);

        DolarApiDTO dolarApiDTO = new DolarApiDTO();
        dolarApiDTO.setSellingPrice(sellingPrice);

        Mockito.when(expenseRepository.findById(id)).thenReturn(Optional.of(existingExpense));
        Mockito.when(dolarApiClient.getRate("oficial")).thenReturn(dolarApiDTO);
        Mockito.when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ExpenseDTO result = expenseService.update(id, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated description", result.getDescription());
        assertEquals(newAmount, result.getAmountInPesos());
        assertEquals(expectedDollars, result.getAmountInDollars());
        Mockito.verify(expenseRepository).findById(id);
        Mockito.verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("Update expense in USD recalculates amountInPesos via historical API")
    public void testUpdateWithUsdCurrencyPastDate() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;
        LocalDate pastDate = LocalDate.of(2024, 8, 20);
        BigDecimal newAmount = new BigDecimal("200");
        BigDecimal sellingPrice = new BigDecimal("1000");
        BigDecimal expectedPesos = newAmount.multiply(sellingPrice);

        Expense existingExpense = new Expense();
        existingExpense.setId(id);
        existingExpense.setDescription("Old USD expense");
        existingExpense.setAmountInDollars(BigDecimal.valueOf(100));
        existingExpense.setDate(pastDate);
        existingExpense.setCategory(category);
        existingExpense.setPaymentMethod(paymentMethod);
        existingExpense.setCurrency(currencyUSD);

        ExpenseDTO updateDTO = new ExpenseDTO();
        updateDTO.setDescription("Updated USD expense");
        updateDTO.setInputAmount(newAmount);
        updateDTO.setDate(pastDate);
        updateDTO.setCategory(modelMapper.map(category, CategoryDTO.class));
        updateDTO.setPaymentMethod(modelMapper.map(paymentMethod, PaymentMethodDTO.class));
        updateDTO.setCurrency(currencyUSD);

        DolarApiHistoricalDTO historicalDTO = new DolarApiHistoricalDTO();
        historicalDTO.setSellingPrice(sellingPrice);

        Mockito.when(expenseRepository.findById(id)).thenReturn(Optional.of(existingExpense));
        Mockito.when(dolarApiHistoricalClient.getRate("oficial", pastDate.toString())).thenReturn(historicalDTO);
        Mockito.when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ExpenseDTO result = expenseService.update(id, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated USD expense", result.getDescription());
        assertEquals(newAmount, result.getAmountInDollars());
        assertEquals(expectedPesos, result.getAmountInPesos());
        Mockito.verify(expenseRepository).findById(id);
        Mockito.verify(expenseRepository).save(any(Expense.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Delete expense removes it from the database")
    public void testDelete() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;

        Expense expense = new Expense();
        expense.setId(id);
        expense.setDescription("To be deleted");
        expense.setAmountInPesos(BigDecimal.valueOf(1000));
        expense.setAmountInDollars(BigDecimal.valueOf(0.67));
        expense.setDate(LocalDate.now());
        expense.setCategory(category);
        expense.setPaymentMethod(paymentMethod);
        expense.setCurrency(currencyARS);

        // Act
        Mockito.when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));
        ExpenseDTO result = expenseService.delete(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        Mockito.verify(expenseRepository).findById(id);
        Mockito.verify(expenseRepository).delete(expense);
    }
}
