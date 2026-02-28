package com.spendwise.unittest;

import com.spendwise.client.dolarApi.DolarApiClient;
import com.spendwise.client.dolarApiHistorical.DolarApiHistoricalClient;
import com.spendwise.client.dolarApiHistorical.DolarApiHistoricalDTO;
import com.spendwise.dto.CategoryDTO;
import com.spendwise.enums.CategoryType;
import com.spendwise.dto.IncomeDTO;
import com.spendwise.dto.IncomeFilterDTO;
import com.spendwise.model.Category;
import com.spendwise.model.Income;
import com.spendwise.repository.IncomeRepository;
import com.spendwise.service.IncomeService;
import com.spendwise.model.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
@DisplayName("Income Unit Tests")
public class IncomeServiceTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private DolarApiClient dolarApiClient;

    @Mock
    private DolarApiHistoricalClient dolarApiHistoricalClient;

    @InjectMocks
    private IncomeService incomeService;

    private User testUser;

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

    private static Category source;
    private static CategoryDTO sourceDTO;

    @BeforeAll
    public static void init() {
        source = new Category();
        source.setId(1L);
        source.setName("Salario");
        source.setEnabled(true);
        source.setType(CategoryType.INCOME);

        sourceDTO = new CategoryDTO();
        sourceDTO.setId(1L);
        sourceDTO.setName("Salario");
        sourceDTO.setEnabled(true);
        sourceDTO.setType(CategoryType.INCOME);
    }

    @Test
    @DisplayName("Create income saves a new income successfully")
    public void testCreate() {
        // Arrange
        LocalDate pastDate = LocalDate.of(2024, 1, 15);
        BigDecimal amountInPesos = new BigDecimal("100000");
        BigDecimal sellingPrice = new BigDecimal("1000");
        BigDecimal expectedDollars = amountInPesos.divide(sellingPrice, 4, RoundingMode.HALF_EVEN);

        IncomeDTO dto = new IncomeDTO();
        dto.setDescription("Monthly salary");
        dto.setAmountInPesos(amountInPesos);
        dto.setSource(sourceDTO);
        dto.setDate(pastDate);

        DolarApiHistoricalDTO historicalDTO = new DolarApiHistoricalDTO();
        historicalDTO.setSellingPrice(sellingPrice);

        Mockito.when(dolarApiHistoricalClient.getRate("oficial", pastDate.toString()))
                .thenReturn(historicalDTO);
        Mockito.when(incomeRepository.save(any(Income.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        IncomeDTO obtained = incomeService.create(dto);

        // Assert
        assertEquals("Monthly salary", obtained.getDescription());
        assertEquals(amountInPesos, obtained.getAmountInPesos());
        assertEquals(expectedDollars, obtained.getAmountInDollars());
        Mockito.verify(incomeRepository).save(any(Income.class));
    }

    @Test
    @DisplayName("Find income by ID returns the income when it exists")
    public void testFindById() throws Exception {
        // Arrange
        Long id = 1L;

        IncomeDTO incomeDTO = new IncomeDTO();
        incomeDTO.setId(id);
        incomeDTO.setDescription("Monthly salary");
        incomeDTO.setAmountInPesos(new BigDecimal("100000"));
        incomeDTO.setAmountInDollars(new BigDecimal("100.0000"));
        incomeDTO.setSource(sourceDTO);
        incomeDTO.setDate(LocalDate.of(2024, 1, 15));

        Income income = modelMapper.map(incomeDTO, Income.class);

        Mockito.when(incomeRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(income));

        // Act
        IncomeDTO obtained = incomeService.findById(id);

        // Assert
        assertEquals(incomeDTO, obtained);
        Mockito.verify(incomeRepository).findByIdAndUser(id, testUser);
        Mockito.verifyNoMoreInteractions(incomeRepository);
    }

    @Test
    @DisplayName("Find income by ID throws exception when income does not exist")
    public void testFindNonExistingById() {
        // Arrange
        Long id = 1L;

        // Act & Assert
        Mockito.when(incomeRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.empty());
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> incomeService.findById(id));
    }

    @Test
    @DisplayName("List all incomes returns complete list")
    public void testList() {
        // Arrange
        Income income1 = new Income();
        income1.setId(1L);
        income1.setDescription("Salario enero");
        income1.setAmountInPesos(new BigDecimal("100000"));
        income1.setAmountInDollars(new BigDecimal("100.0000"));
        income1.setSource(source);
        income1.setDate(LocalDate.of(2024, 1, 15));

        Income income2 = new Income();
        income2.setId(2L);
        income2.setDescription("Salario febrero");
        income2.setAmountInPesos(new BigDecimal("110000"));
        income2.setAmountInDollars(new BigDecimal("110.0000"));
        income2.setSource(source);
        income2.setDate(LocalDate.of(2024, 2, 15));

        List<Income> incomes = Arrays.asList(income1, income2);
        Page<Income> incomePage = new PageImpl<>(incomes);

        Pageable pageable = PageRequest.of(0, 20);
        IncomeFilterDTO filters = new IncomeFilterDTO();

        Mockito.when(incomeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(incomePage);

        // Act
        Page<IncomeDTO> obtained = incomeService.list(filters, pageable);

        // Assert
        assertEquals(2, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals("Salario enero", obtained.getContent().get(0).getDescription());
        assertEquals("Salario febrero", obtained.getContent().get(1).getDescription());
    }

    @Test
    @DisplayName("Update income modifies existing income successfully")
    public void testUpdate() throws Exception {
        // Arrange
        Long id = 1L;
        LocalDate pastDate = LocalDate.of(2024, 1, 15);
        BigDecimal amountInPesos = new BigDecimal("120000");
        BigDecimal sellingPrice = new BigDecimal("1000");
        BigDecimal expectedDollars = amountInPesos.divide(sellingPrice, 4, RoundingMode.HALF_EVEN);

        Income existingIncome = new Income();
        existingIncome.setId(id);
        existingIncome.setDescription("Old description");
        existingIncome.setAmountInPesos(new BigDecimal("100000"));
        existingIncome.setSource(source);
        existingIncome.setDate(pastDate);

        IncomeDTO newDTO = new IncomeDTO();
        newDTO.setDescription("Updated salary");
        newDTO.setAmountInPesos(amountInPesos);
        newDTO.setSource(sourceDTO);
        newDTO.setDate(pastDate);

        DolarApiHistoricalDTO historicalDTO = new DolarApiHistoricalDTO();
        historicalDTO.setSellingPrice(sellingPrice);

        Mockito.when(incomeRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(existingIncome));
        Mockito.when(dolarApiHistoricalClient.getRate("oficial", pastDate.toString()))
                .thenReturn(historicalDTO);
        Mockito.when(incomeRepository.save(any(Income.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        IncomeDTO obtained = incomeService.update(id, newDTO);

        // Assert
        assertEquals("Updated salary", obtained.getDescription());
        assertEquals(amountInPesos, obtained.getAmountInPesos());
        assertEquals(expectedDollars, obtained.getAmountInDollars());
        Mockito.verify(incomeRepository).findByIdAndUser(id, testUser);
        Mockito.verify(incomeRepository).save(any(Income.class));
    }

    @Test
    @DisplayName("Delete income removes it from the database")
    public void testDelete() throws Exception {
        // Arrange
        Long id = 1L;

        IncomeDTO incomeDTO = new IncomeDTO();
        incomeDTO.setId(id);
        incomeDTO.setDescription("Monthly salary");
        incomeDTO.setAmountInPesos(new BigDecimal("100000"));
        incomeDTO.setAmountInDollars(new BigDecimal("100.0000"));
        incomeDTO.setSource(sourceDTO);
        incomeDTO.setDate(LocalDate.of(2024, 1, 15));

        Income income = modelMapper.map(incomeDTO, Income.class);

        Mockito.when(incomeRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(income));

        // Act
        IncomeDTO deleted = incomeService.delete(id);

        // Assert
        assertEquals(incomeDTO, deleted);
        Mockito.verify(incomeRepository).findByIdAndUser(id, testUser);
        Mockito.verify(incomeRepository).delete(income);
        Mockito.verifyNoMoreInteractions(incomeRepository);
    }

    @Test
    @DisplayName("List incomes with description filter")
    public void testListWithDescriptionFilter() {
        // Arrange
        Income income1 = new Income();
        income1.setId(1L);
        income1.setDescription("Salario enero");
        income1.setAmountInPesos(new BigDecimal("100000"));
        income1.setSource(source);
        income1.setDate(LocalDate.of(2024, 1, 15));

        List<Income> incomes = Arrays.asList(income1);
        Page<Income> incomePage = new PageImpl<>(incomes);

        Pageable pageable = PageRequest.of(0, 20);
        IncomeFilterDTO filters = new IncomeFilterDTO();
        filters.setDescription("Salario");

        Mockito.when(incomeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(incomePage);

        // Act
        Page<IncomeDTO> obtained = incomeService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Salario enero", obtained.getContent().get(0).getDescription());
    }

    @Test
    @DisplayName("List incomes with amount in pesos filter")
    public void testListWithAmountInPesosFilter() {
        // Arrange
        BigDecimal amount = new BigDecimal("100000");

        Income income1 = new Income();
        income1.setId(1L);
        income1.setDescription("Salario enero");
        income1.setAmountInPesos(amount);
        income1.setSource(source);
        income1.setDate(LocalDate.of(2024, 1, 15));

        List<Income> incomes = Arrays.asList(income1);
        Page<Income> incomePage = new PageImpl<>(incomes);

        Pageable pageable = PageRequest.of(0, 20);
        IncomeFilterDTO filters = new IncomeFilterDTO();
        filters.setAmountInPesos(amount);

        Mockito.when(incomeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(incomePage);

        // Act
        Page<IncomeDTO> obtained = incomeService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals(amount, obtained.getContent().get(0).getAmountInPesos());
    }

    @Test
    @DisplayName("List incomes with amount in dollars filter")
    public void testListWithAmountInDollarsFilter() {
        // Arrange
        BigDecimal amountInDollars = new BigDecimal("100.0000");

        Income income1 = new Income();
        income1.setId(1L);
        income1.setDescription("Salario enero");
        income1.setAmountInPesos(new BigDecimal("100000"));
        income1.setAmountInDollars(amountInDollars);
        income1.setSource(source);
        income1.setDate(LocalDate.of(2024, 1, 15));

        List<Income> incomes = Arrays.asList(income1);
        Page<Income> incomePage = new PageImpl<>(incomes);

        Pageable pageable = PageRequest.of(0, 20);
        IncomeFilterDTO filters = new IncomeFilterDTO();
        filters.setAmountInDollars(amountInDollars);

        Mockito.when(incomeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(incomePage);

        // Act
        Page<IncomeDTO> obtained = incomeService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals(amountInDollars, obtained.getContent().get(0).getAmountInDollars());
    }

    @Test
    @DisplayName("List incomes with source filter")
    public void testListWithSourceFilter() {
        // Arrange
        Income income1 = new Income();
        income1.setId(1L);
        income1.setDescription("Salario enero");
        income1.setAmountInPesos(new BigDecimal("100000"));
        income1.setSource(source);
        income1.setDate(LocalDate.of(2024, 1, 15));

        List<Income> incomes = Arrays.asList(income1);
        Page<Income> incomePage = new PageImpl<>(incomes);

        Pageable pageable = PageRequest.of(0, 20);
        IncomeFilterDTO filters = new IncomeFilterDTO();
        filters.setSource(sourceDTO);

        Mockito.when(incomeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(incomePage);

        // Act
        Page<IncomeDTO> obtained = incomeService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals(sourceDTO.getId(), obtained.getContent().get(0).getSource().getId());
    }

    @Test
    @DisplayName("List incomes with date filter")
    public void testListWithDateFilter() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);

        Income income1 = new Income();
        income1.setId(1L);
        income1.setDescription("Salario enero");
        income1.setAmountInPesos(new BigDecimal("100000"));
        income1.setSource(source);
        income1.setDate(date);

        List<Income> incomes = Arrays.asList(income1);
        Page<Income> incomePage = new PageImpl<>(incomes);

        Pageable pageable = PageRequest.of(0, 20);
        IncomeFilterDTO filters = new IncomeFilterDTO();
        filters.setDate(date);

        Mockito.when(incomeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(incomePage);

        // Act
        Page<IncomeDTO> obtained = incomeService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals(date, obtained.getContent().get(0).getDate());
    }

    @Test
    @DisplayName("List incomes with multiple filters")
    public void testListWithMultipleFilters() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);

        Income income1 = new Income();
        income1.setId(1L);
        income1.setDescription("Salario enero");
        income1.setAmountInPesos(new BigDecimal("100000"));
        income1.setSource(source);
        income1.setDate(date);

        List<Income> incomes = Arrays.asList(income1);
        Page<Income> incomePage = new PageImpl<>(incomes);

        Pageable pageable = PageRequest.of(0, 20);
        IncomeFilterDTO filters = new IncomeFilterDTO();
        filters.setDescription("Salario");
        filters.setDate(date);

        Mockito.when(incomeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(incomePage);

        // Act
        Page<IncomeDTO> obtained = incomeService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Salario enero", obtained.getContent().get(0).getDescription());
        assertEquals(date, obtained.getContent().get(0).getDate());
    }

    @Test
    @DisplayName("List incomes with no results")
    public void testListWithNoResults() {
        // Arrange
        List<Income> incomes = Collections.emptyList();
        Page<Income> incomePage = new PageImpl<>(incomes);

        Pageable pageable = PageRequest.of(0, 20);
        IncomeFilterDTO filters = new IncomeFilterDTO();
        filters.setDescription("NoExiste");

        Mockito.when(incomeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(incomePage);

        // Act
        Page<IncomeDTO> obtained = incomeService.list(filters, pageable);

        // Assert
        assertEquals(0, obtained.getTotalElements());
        assertTrue(obtained.getContent().isEmpty());
    }

    @Test
    @DisplayName("List incomes with pagination")
    public void testListWithPagination() {
        // Arrange
        Income income1 = new Income();
        income1.setId(1L);
        income1.setDescription("Salario enero");
        income1.setAmountInPesos(new BigDecimal("100000"));
        income1.setSource(source);
        income1.setDate(LocalDate.of(2024, 1, 15));

        Income income2 = new Income();
        income2.setId(2L);
        income2.setDescription("Salario febrero");
        income2.setAmountInPesos(new BigDecimal("110000"));
        income2.setSource(source);
        income2.setDate(LocalDate.of(2024, 2, 15));

        List<Income> incomes = Arrays.asList(income1, income2);
        Page<Income> incomePage = new PageImpl<>(incomes, PageRequest.of(0, 10), 25);

        Pageable pageable = PageRequest.of(0, 10);
        IncomeFilterDTO filters = new IncomeFilterDTO();

        Mockito.when(incomeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(incomePage);

        // Act
        Page<IncomeDTO> obtained = incomeService.list(filters, pageable);

        // Assert
        assertEquals(25, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals(3, obtained.getTotalPages());
        assertEquals(0, obtained.getNumber());
    }

    @Test
    @DisplayName("List incomes with custom page size")
    public void testListWithCustomPageSize() {
        // Arrange
        Income income1 = new Income();
        income1.setId(1L);
        income1.setDescription("Salario enero");
        income1.setAmountInPesos(new BigDecimal("100000"));
        income1.setSource(source);
        income1.setDate(LocalDate.of(2024, 1, 15));

        List<Income> incomes = Arrays.asList(income1);
        Page<Income> incomePage = new PageImpl<>(incomes);

        Pageable pageable = PageRequest.of(0, 5);
        IncomeFilterDTO filters = new IncomeFilterDTO();

        Mockito.when(incomeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(incomePage);

        // Act
        Page<IncomeDTO> obtained = incomeService.list(filters, pageable);

        // Assert
        assertEquals(5, pageable.getPageSize());
        assertEquals(1, obtained.getContent().size());
    }

}