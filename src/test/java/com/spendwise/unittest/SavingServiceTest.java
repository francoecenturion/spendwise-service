package com.spendwise.unittest;

import com.spendwise.client.dolarApi.DolarApiClient;
import com.spendwise.client.dolarApi.DolarApiDTO;
import com.spendwise.client.dolarApiHistorical.DolarApiHistoricalClient;
import com.spendwise.client.dolarApiHistorical.DolarApiHistoricalDTO;
import com.spendwise.dto.SavingDTO;
import com.spendwise.dto.SavingFilterDTO;
import com.spendwise.model.Currency;
import com.spendwise.model.Saving;
import com.spendwise.repository.SavingRepository;
import com.spendwise.service.SavingService;
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
@DisplayName("Saving Unit Tests")
public class SavingServiceTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private SavingRepository savingRespository;

    @Mock
    private DolarApiClient dolarApiClient;

    @Mock
    private DolarApiHistoricalClient dolarApiHistoricalClient;

    @InjectMocks
    private SavingService savingService;

    private static Currency currency;

    @BeforeAll
    public static void init() {
        currency = new Currency();
        currency.setId(1L);
        currency.setName("Peso Argentino");
        currency.setSymbol("ARS");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Create saving with a past date uses DolarApiHistoricalClient to calculate dollars")
    public void testCreateWithPastDate() {
        // Arrange
        LocalDate pastDate = LocalDate.of(2024, 6, 15);
        BigDecimal amountInPesos = new BigDecimal("500000");
        BigDecimal sellingPrice = new BigDecimal("900");
        BigDecimal expectedDollars = amountInPesos.divide(sellingPrice, 4, RoundingMode.HALF_EVEN);

        SavingDTO dto = new SavingDTO();
        dto.setDescription("Ahorro junio");
        dto.setInputAmount(amountInPesos);
        dto.setCurrency(currency);
        dto.setDate(pastDate);

        DolarApiHistoricalDTO historicalDTO = new DolarApiHistoricalDTO();
        historicalDTO.setSellingPrice(sellingPrice);

        Mockito.when(dolarApiHistoricalClient.getRate("oficial", pastDate.toString()))
                .thenReturn(historicalDTO);
        Mockito.when(savingRespository.save(any(Saving.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        SavingDTO result = savingService.create(dto);

        // Assert
        assertEquals("Ahorro junio", result.getDescription());
        assertEquals(amountInPesos, result.getAmountInPesos());
        assertEquals(expectedDollars, result.getAmountInDollars());
        Mockito.verify(dolarApiHistoricalClient).getRate("oficial", pastDate.toString());
        Mockito.verify(savingRespository).save(any(Saving.class));
    }

    @Test
    @DisplayName("Create saving with today's date uses DolarApiClient to calculate dollars")
    public void testCreateWithTodaysDate() {
        // Arrange
        LocalDate today = LocalDate.now();
        BigDecimal amountInPesos = new BigDecimal("300000");
        BigDecimal sellingPrice = new BigDecimal("1200");
        BigDecimal expectedDollars = amountInPesos.divide(sellingPrice, 4, RoundingMode.HALF_EVEN);

        SavingDTO dto = new SavingDTO();
        dto.setDescription("Ahorro hoy");
        dto.setInputAmount(amountInPesos);
        dto.setCurrency(currency);
        dto.setDate(today);

        DolarApiDTO dolarApiDTO = new DolarApiDTO();
        dolarApiDTO.setSellingPrice(sellingPrice);

        Mockito.when(dolarApiClient.getRate("oficial")).thenReturn(dolarApiDTO);
        Mockito.when(savingRespository.save(any(Saving.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        SavingDTO result = savingService.create(dto);

        // Assert
        assertEquals("Ahorro hoy", result.getDescription());
        assertEquals(amountInPesos, result.getAmountInPesos());
        assertEquals(expectedDollars, result.getAmountInDollars());
        Mockito.verify(dolarApiClient).getRate("oficial");
        Mockito.verify(savingRespository).save(any(Saving.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FIND BY ID
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Find saving by ID returns the saving when it exists")
    public void testFindById() throws Exception {
        // Arrange
        Long id = 1L;
        Saving saving = new Saving();
        saving.setId(id);
        saving.setDescription("Ahorro enero");
        saving.setAmountInPesos(new BigDecimal("500000"));
        saving.setAmountInDollars(new BigDecimal("500.0000"));
        saving.setCurrency(currency);
        saving.setDate(LocalDate.of(2024, 1, 10));

        Mockito.when(savingRespository.findById(id)).thenReturn(Optional.of(saving));

        // Act
        SavingDTO result = savingService.findById(id);

        // Assert
        assertEquals(id, result.getId());
        assertEquals("Ahorro enero", result.getDescription());
        Mockito.verify(savingRespository).findById(id);
        Mockito.verifyNoMoreInteractions(savingRespository);
    }

    @Test
    @DisplayName("Find saving by ID throws NotFoundException when saving does not exist")
    public void testFindByIdNotFound() {
        // Arrange
        Long id = 99L;
        Mockito.when(savingRespository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> savingService.findById(id));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LIST
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("List all savings returns complete list")
    public void testList() {
        // Arrange
        Saving saving1 = new Saving();
        saving1.setId(1L);
        saving1.setDescription("Ahorro enero");
        saving1.setAmountInPesos(new BigDecimal("500000"));
        saving1.setAmountInDollars(new BigDecimal("500.0000"));
        saving1.setCurrency(currency);
        saving1.setDate(LocalDate.of(2024, 1, 10));

        Saving saving2 = new Saving();
        saving2.setId(2L);
        saving2.setDescription("Ahorro febrero");
        saving2.setAmountInPesos(new BigDecimal("600000"));
        saving2.setAmountInDollars(new BigDecimal("600.0000"));
        saving2.setCurrency(currency);
        saving2.setDate(LocalDate.of(2024, 2, 10));

        Page<Saving> page = new PageImpl<>(Arrays.asList(saving1, saving2));
        Pageable pageable = PageRequest.of(0, 20);
        SavingFilterDTO filters = new SavingFilterDTO();

        Mockito.when(savingRespository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingDTO> result = savingService.list(filters, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals("Ahorro enero", result.getContent().get(0).getDescription());
        assertEquals("Ahorro febrero", result.getContent().get(1).getDescription());
    }

    @Test
    @DisplayName("List savings filtered by description")
    public void testListWithDescriptionFilter() {
        // Arrange
        Saving saving = new Saving();
        saving.setId(1L);
        saving.setDescription("Ahorro enero");
        saving.setAmountInPesos(new BigDecimal("500000"));
        saving.setCurrency(currency);
        saving.setDate(LocalDate.of(2024, 1, 10));

        Page<Saving> page = new PageImpl<>(List.of(saving));
        Pageable pageable = PageRequest.of(0, 20);
        SavingFilterDTO filters = new SavingFilterDTO();
        filters.setDescription("enero");

        Mockito.when(savingRespository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingDTO> result = savingService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Ahorro enero", result.getContent().get(0).getDescription());
    }

    @Test
    @DisplayName("List savings filtered by minimum amount in pesos")
    public void testListWithMinAmountInPesosFilter() {
        // Arrange
        Saving saving = new Saving();
        saving.setId(1L);
        saving.setDescription("Ahorro grande");
        saving.setAmountInPesos(new BigDecimal("800000"));
        saving.setCurrency(currency);
        saving.setDate(LocalDate.of(2024, 3, 1));

        Page<Saving> page = new PageImpl<>(List.of(saving));
        Pageable pageable = PageRequest.of(0, 20);
        SavingFilterDTO filters = new SavingFilterDTO();
        filters.setMinAmountInPesos(new BigDecimal("500000"));

        Mockito.when(savingRespository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingDTO> result = savingService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getAmountInPesos().compareTo(new BigDecimal("500000")) >= 0);
    }

    @Test
    @DisplayName("List savings filtered by maximum amount in pesos")
    public void testListWithMaxAmountInPesosFilter() {
        // Arrange
        Saving saving = new Saving();
        saving.setId(1L);
        saving.setDescription("Ahorro chico");
        saving.setAmountInPesos(new BigDecimal("200000"));
        saving.setCurrency(currency);
        saving.setDate(LocalDate.of(2024, 3, 1));

        Page<Saving> page = new PageImpl<>(List.of(saving));
        Pageable pageable = PageRequest.of(0, 20);
        SavingFilterDTO filters = new SavingFilterDTO();
        filters.setMaxAmountInPesos(new BigDecimal("300000"));

        Mockito.when(savingRespository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingDTO> result = savingService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getAmountInPesos().compareTo(new BigDecimal("300000")) <= 0);
    }

    @Test
    @DisplayName("List savings filtered by amount in dollars range")
    public void testListWithAmountInDollarsRangeFilter() {
        // Arrange
        Saving saving = new Saving();
        saving.setId(1L);
        saving.setDescription("Ahorro en dólares");
        saving.setAmountInPesos(new BigDecimal("500000"));
        saving.setAmountInDollars(new BigDecimal("400.0000"));
        saving.setCurrency(currency);
        saving.setDate(LocalDate.of(2024, 4, 1));

        Page<Saving> page = new PageImpl<>(List.of(saving));
        Pageable pageable = PageRequest.of(0, 20);
        SavingFilterDTO filters = new SavingFilterDTO();
        filters.setMinAmountInDollars(new BigDecimal("300"));
        filters.setMaxAmountInDollars(new BigDecimal("500"));

        Mockito.when(savingRespository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingDTO> result = savingService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getAmountInDollars().compareTo(new BigDecimal("300")) >= 0);
        assertTrue(result.getContent().get(0).getAmountInDollars().compareTo(new BigDecimal("500")) <= 0);
    }

    @Test
    @DisplayName("List savings filtered by date range")
    public void testListWithDateRangeFilter() {
        // Arrange
        Saving saving = new Saving();
        saving.setId(1L);
        saving.setDescription("Ahorro marzo");
        saving.setAmountInPesos(new BigDecimal("500000"));
        saving.setCurrency(currency);
        saving.setDate(LocalDate.of(2024, 3, 15));

        Page<Saving> page = new PageImpl<>(List.of(saving));
        Pageable pageable = PageRequest.of(0, 20);
        SavingFilterDTO filters = new SavingFilterDTO();
        filters.setStartDate(LocalDate.of(2024, 3, 1));
        filters.setEndDate(LocalDate.of(2024, 3, 31));

        Mockito.when(savingRespository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingDTO> result = savingService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertFalse(result.getContent().get(0).getDate().isBefore(LocalDate.of(2024, 3, 1)));
        assertFalse(result.getContent().get(0).getDate().isAfter(LocalDate.of(2024, 3, 31)));
    }

    @Test
    @DisplayName("List savings filtered by currency ID")
    public void testListWithCurrencyFilter() {
        // Arrange
        Saving saving = new Saving();
        saving.setId(1L);
        saving.setDescription("Ahorro ARS");
        saving.setAmountInPesos(new BigDecimal("500000"));
        saving.setCurrency(currency);
        saving.setDate(LocalDate.of(2024, 1, 10));

        Page<Saving> page = new PageImpl<>(List.of(saving));
        Pageable pageable = PageRequest.of(0, 20);
        SavingFilterDTO filters = new SavingFilterDTO();
        filters.setCurrencyId(1L);

        Mockito.when(savingRespository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingDTO> result = savingService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getCurrency().getId());
    }

    @Test
    @DisplayName("List savings with multiple filters combined")
    public void testListWithMultipleFilters() {
        // Arrange
        Saving saving = new Saving();
        saving.setId(1L);
        saving.setDescription("Ahorro especial");
        saving.setAmountInPesos(new BigDecimal("750000"));
        saving.setAmountInDollars(new BigDecimal("600.0000"));
        saving.setCurrency(currency);
        saving.setDate(LocalDate.of(2024, 5, 20));

        Page<Saving> page = new PageImpl<>(List.of(saving));
        Pageable pageable = PageRequest.of(0, 20);
        SavingFilterDTO filters = new SavingFilterDTO();
        filters.setDescription("especial");
        filters.setMinAmountInPesos(new BigDecimal("500000"));
        filters.setStartDate(LocalDate.of(2024, 5, 1));
        filters.setCurrencyId(1L);

        Mockito.when(savingRespository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingDTO> result = savingService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Ahorro especial", result.getContent().get(0).getDescription());
        assertEquals(1L, result.getContent().get(0).getCurrency().getId());
    }

    @Test
    @DisplayName("List savings returns empty page when no results match")
    public void testListNoResults() {
        // Arrange
        Page<Saving> page = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 20);
        SavingFilterDTO filters = new SavingFilterDTO();
        filters.setDescription("Inexistente");

        Mockito.when(savingRespository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingDTO> result = savingService.list(filters, pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("List savings with pagination returns correct metadata")
    public void testListWithPagination() {
        // Arrange
        Saving saving1 = new Saving();
        saving1.setId(1L);
        saving1.setDescription("Ahorro 1");
        saving1.setAmountInPesos(new BigDecimal("100000"));
        saving1.setCurrency(currency);
        saving1.setDate(LocalDate.of(2024, 1, 1));

        Saving saving2 = new Saving();
        saving2.setId(2L);
        saving2.setDescription("Ahorro 2");
        saving2.setAmountInPesos(new BigDecimal("200000"));
        saving2.setCurrency(currency);
        saving2.setDate(LocalDate.of(2024, 2, 1));

        Pageable pageable = PageRequest.of(0, 2);
        Page<Saving> page = new PageImpl<>(Arrays.asList(saving1, saving2), pageable, 10);
        SavingFilterDTO filters = new SavingFilterDTO();

        Mockito.when(savingRespository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingDTO> result = savingService.list(filters, pageable);

        // Assert
        assertEquals(10, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalPages());
        assertEquals(0, result.getNumber());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Update saving recalculates amountInDollars via historical API")
    public void testUpdate() throws Exception {
        // Arrange
        Long id = 1L;
        LocalDate pastDate = LocalDate.of(2024, 6, 1);
        BigDecimal newAmount = new BigDecimal("900000");
        BigDecimal sellingPrice = new BigDecimal("1000");
        BigDecimal expectedDollars = newAmount.divide(sellingPrice, 4, RoundingMode.HALF_EVEN);

        Saving existing = new Saving();
        existing.setId(id);
        existing.setDescription("Ahorro viejo");
        existing.setAmountInPesos(new BigDecimal("500000"));
        existing.setCurrency(currency);
        existing.setDate(pastDate);

        SavingDTO updateDTO = new SavingDTO();
        updateDTO.setDescription("Ahorro actualizado");
        updateDTO.setInputAmount(newAmount);
        updateDTO.setCurrency(currency);
        updateDTO.setDate(pastDate);

        DolarApiHistoricalDTO historicalDTO = new DolarApiHistoricalDTO();
        historicalDTO.setSellingPrice(sellingPrice);

        Mockito.when(savingRespository.findById(id)).thenReturn(Optional.of(existing));
        Mockito.when(dolarApiHistoricalClient.getRate("oficial", pastDate.toString()))
                .thenReturn(historicalDTO);
        Mockito.when(savingRespository.save(any(Saving.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        SavingDTO result = savingService.update(id, updateDTO);

        // Assert
        assertEquals("Ahorro actualizado", result.getDescription());
        assertEquals(newAmount, result.getAmountInPesos());
        assertEquals(expectedDollars, result.getAmountInDollars());
        Mockito.verify(savingRespository).findById(id);
        Mockito.verify(savingRespository).save(any(Saving.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Delete saving removes it from the repository")
    public void testDelete() throws Exception {
        // Arrange
        Long id = 1L;
        Saving saving = new Saving();
        saving.setId(id);
        saving.setDescription("Ahorro a eliminar");
        saving.setAmountInPesos(new BigDecimal("500000"));
        saving.setCurrency(currency);
        saving.setDate(LocalDate.of(2024, 1, 10));

        Mockito.when(savingRespository.findById(id)).thenReturn(Optional.of(saving));

        // Act
        SavingDTO result = savingService.delete(id);

        // Assert
        assertEquals("Ahorro a eliminar", result.getDescription());
        Mockito.verify(savingRespository).findById(id);
        Mockito.verify(savingRespository).delete(saving);
        Mockito.verifyNoMoreInteractions(savingRespository);
    }
}
