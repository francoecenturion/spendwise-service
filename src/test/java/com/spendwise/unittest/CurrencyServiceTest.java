package com.spendwise.unittest;

import com.spendwise.dto.CurrencyDTO;
import com.spendwise.dto.CurrencyFilterDTO;
import com.spendwise.model.Currency;
import com.spendwise.repository.CurrencyRepository;
import com.spendwise.service.CurrencyService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("Currency Unit Tests")
public class CurrencyServiceTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    @DisplayName("Create currency saves a new currency with enabled=true")
    public void testCreate() {
        // Arrange
        CurrencyDTO dto = new CurrencyDTO();
        dto.setName("Peso Argentino");
        dto.setSymbol("$");

        Currency currency = new Currency();
        currency.setName("Peso Argentino");
        currency.setSymbol("$");
        currency.setEnabled(true);

        Mockito.when(currencyRepository.save(currency)).thenReturn(currency);

        // Act
        CurrencyDTO obtained = currencyService.create(dto);

        // Assert
        assertEquals(dto.getName(), obtained.getName());
        assertEquals(dto.getSymbol(), obtained.getSymbol());
        Mockito.verify(currencyRepository).save(currency);
        Mockito.verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    @DisplayName("Find currency by ID returns the currency when it exists")
    public void testFindById() throws Exception {
        // Arrange
        Long id = 1L;

        CurrencyDTO currencyDTO = new CurrencyDTO();
        currencyDTO.setId(id);
        currencyDTO.setName("Peso Argentino");
        currencyDTO.setSymbol("$");

        Currency currency = modelMapper.map(currencyDTO, Currency.class);

        Mockito.when(currencyRepository.findById(id)).thenReturn(Optional.of(currency));

        // Act
        CurrencyDTO obtained = currencyService.findById(id);

        // Assert
        assertEquals(currencyDTO, obtained);
        Mockito.verify(currencyRepository).findById(id);
        Mockito.verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    @DisplayName("Find currency by ID throws exception when currency does not exist")
    public void testFindNonExistingById() {
        // Arrange
        Long id = 1L;

        // Act & Assert
        Mockito.when(currencyRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> currencyService.findById(id));
    }

    @Test
    @DisplayName("List all currencies returns complete list")
    public void testList() {
        // Arrange
        Currency currency1 = new Currency();
        currency1.setId(1L);
        currency1.setName("Peso Argentino");
        currency1.setSymbol("$");
        currency1.setEnabled(true);

        Currency currency2 = new Currency();
        currency2.setId(2L);
        currency2.setName("Dólar");
        currency2.setSymbol("U");
        currency2.setEnabled(true);

        List<Currency> currencies = Arrays.asList(currency1, currency2);
        Page<Currency> currencyPage = new PageImpl<>(currencies);

        Pageable pageable = PageRequest.of(0, 20);
        CurrencyFilterDTO filters = new CurrencyFilterDTO();

        Mockito.when(currencyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(currencyPage);

        // Act
        Page<CurrencyDTO> obtained = currencyService.list(filters, pageable);

        // Assert
        assertEquals(2, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals("Peso Argentino", obtained.getContent().get(0).getName());
        assertEquals("Dólar", obtained.getContent().get(1).getName());
    }

    @Test
    @DisplayName("Update currency modifies name and symbol successfully")
    public void testUpdate() throws Exception {
        // Arrange
        Long id = 1L;

        CurrencyDTO currentDTO = new CurrencyDTO();
        currentDTO.setId(id);
        currentDTO.setName("Peso Argentino");
        currentDTO.setSymbol("$");

        CurrencyDTO newDTO = new CurrencyDTO();
        newDTO.setId(id);
        newDTO.setName("Peso Argentino Actualizado");
        newDTO.setSymbol("P");

        Currency currency = modelMapper.map(currentDTO, Currency.class);

        Mockito.when(currencyRepository.findById(id)).thenReturn(Optional.of(currency));
        Mockito.when(currencyRepository.save(any(Currency.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CurrencyDTO obtained = currencyService.update(id, newDTO);

        // Assert
        assertEquals(newDTO, obtained);
        Mockito.verify(currencyRepository).findById(id);
        Mockito.verify(currencyRepository).save(currency);
        Mockito.verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    @DisplayName("Delete currency removes it from the database")
    public void testDelete() throws Exception {
        // Arrange
        Long id = 1L;

        CurrencyDTO currencyDTO = new CurrencyDTO();
        currencyDTO.setId(id);
        currencyDTO.setName("Peso Argentino");
        currencyDTO.setSymbol("$");

        Currency currency = modelMapper.map(currencyDTO, Currency.class);

        Mockito.when(currencyRepository.findById(id)).thenReturn(Optional.of(currency));

        // Act
        CurrencyDTO deleted = currencyService.delete(id);

        // Assert
        assertEquals(currencyDTO, deleted);
        Mockito.verify(currencyRepository).findById(id);
        Mockito.verify(currencyRepository).delete(currency);
        Mockito.verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    @DisplayName("Disable currency sets enabled flag to false")
    public void testDisable() throws Exception {
        // Arrange
        Long id = 1L;

        CurrencyDTO currencyDTO = new CurrencyDTO();
        currencyDTO.setId(id);
        currencyDTO.setName("Peso Argentino");
        currencyDTO.setSymbol("$");

        Currency currency = modelMapper.map(currencyDTO, Currency.class);
        currency.setEnabled(true);

        Mockito.when(currencyRepository.findById(id)).thenReturn(Optional.of(currency));
        Mockito.when(currencyRepository.save(any(Currency.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CurrencyDTO obtained = currencyService.disable(id);

        // Assert
        currencyDTO.setEnabled(false);
        assertEquals(currencyDTO, obtained);
        assertFalse(currency.getEnabled());
        Mockito.verify(currencyRepository).findById(id);
        Mockito.verify(currencyRepository).save(currency);
        Mockito.verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    @DisplayName("Enable currency sets enabled flag to true")
    public void testEnable() throws Exception {
        // Arrange
        Long id = 1L;

        CurrencyDTO currencyDTO = new CurrencyDTO();
        currencyDTO.setId(id);
        currencyDTO.setName("Peso Argentino");
        currencyDTO.setSymbol("$");

        Currency currency = modelMapper.map(currencyDTO, Currency.class);
        currency.setEnabled(false);

        Mockito.when(currencyRepository.findById(id)).thenReturn(Optional.of(currency));
        Mockito.when(currencyRepository.save(any(Currency.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CurrencyDTO obtained = currencyService.enable(id);

        // Assert
        currencyDTO.setEnabled(true);
        assertEquals(currencyDTO, obtained);
        assertTrue(currency.getEnabled());
        Mockito.verify(currencyRepository).findById(id);
        Mockito.verify(currencyRepository).save(currency);
        Mockito.verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    @DisplayName("List currencies with name filter")
    public void testListWithNameFilter() {
        // Arrange
        Currency currency1 = new Currency();
        currency1.setId(1L);
        currency1.setName("Peso Argentino");
        currency1.setSymbol("$");
        currency1.setEnabled(true);

        List<Currency> currencies = Arrays.asList(currency1);
        Page<Currency> currencyPage = new PageImpl<>(currencies);

        Pageable pageable = PageRequest.of(0, 20);
        CurrencyFilterDTO filters = new CurrencyFilterDTO();
        filters.setName("Peso");

        Mockito.when(currencyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(currencyPage);

        // Act
        Page<CurrencyDTO> obtained = currencyService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Peso Argentino", obtained.getContent().get(0).getName());
    }

    @Test
    @DisplayName("List currencies with enabled filter true")
    public void testListWithEnabledFilterTrue() {
        // Arrange
        Currency currency1 = new Currency();
        currency1.setId(1L);
        currency1.setName("Peso Argentino");
        currency1.setSymbol("$");
        currency1.setEnabled(true);

        List<Currency> currencies = Arrays.asList(currency1);
        Page<Currency> currencyPage = new PageImpl<>(currencies);

        Pageable pageable = PageRequest.of(0, 20);
        CurrencyFilterDTO filters = new CurrencyFilterDTO();
        filters.setEnabled(true);

        Mockito.when(currencyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(currencyPage);

        // Act
        Page<CurrencyDTO> obtained = currencyService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Peso Argentino", obtained.getContent().get(0).getName());
    }

    @Test
    @DisplayName("List currencies with enabled filter false")
    public void testListWithEnabledFilterFalse() {
        // Arrange
        Currency currency1 = new Currency();
        currency1.setId(1L);
        currency1.setName("Euro");
        currency1.setSymbol("E");
        currency1.setEnabled(false);

        List<Currency> currencies = Arrays.asList(currency1);
        Page<Currency> currencyPage = new PageImpl<>(currencies);

        Pageable pageable = PageRequest.of(0, 20);
        CurrencyFilterDTO filters = new CurrencyFilterDTO();
        filters.setEnabled(false);

        Mockito.when(currencyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(currencyPage);

        // Act
        Page<CurrencyDTO> obtained = currencyService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Euro", obtained.getContent().get(0).getName());
    }

    @Test
    @DisplayName("List currencies with multiple filters")
    public void testListWithMultipleFilters() {
        // Arrange
        Currency currency1 = new Currency();
        currency1.setId(1L);
        currency1.setName("Peso Argentino");
        currency1.setSymbol("$");
        currency1.setEnabled(true);

        List<Currency> currencies = Arrays.asList(currency1);
        Page<Currency> currencyPage = new PageImpl<>(currencies);

        Pageable pageable = PageRequest.of(0, 20);
        CurrencyFilterDTO filters = new CurrencyFilterDTO();
        filters.setName("Peso");
        filters.setEnabled(true);

        Mockito.when(currencyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(currencyPage);

        // Act
        Page<CurrencyDTO> obtained = currencyService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Peso Argentino", obtained.getContent().get(0).getName());
    }

    @Test
    @DisplayName("List currencies with no results")
    public void testListWithNoResults() {
        // Arrange
        List<Currency> currencies = Collections.emptyList();
        Page<Currency> currencyPage = new PageImpl<>(currencies);

        Pageable pageable = PageRequest.of(0, 20);
        CurrencyFilterDTO filters = new CurrencyFilterDTO();
        filters.setName("NoExiste");

        Mockito.when(currencyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(currencyPage);

        // Act
        Page<CurrencyDTO> obtained = currencyService.list(filters, pageable);

        // Assert
        assertEquals(0, obtained.getTotalElements());
        assertTrue(obtained.getContent().isEmpty());
    }

    @Test
    @DisplayName("List currencies with pagination")
    public void testListWithPagination() {
        // Arrange
        Currency currency1 = new Currency();
        currency1.setId(1L);
        currency1.setName("Peso Argentino");
        currency1.setSymbol("$");
        currency1.setEnabled(true);

        Currency currency2 = new Currency();
        currency2.setId(2L);
        currency2.setName("Dólar");
        currency2.setSymbol("U");
        currency2.setEnabled(true);

        List<Currency> currencies = Arrays.asList(currency1, currency2);
        Page<Currency> currencyPage = new PageImpl<>(currencies, PageRequest.of(0, 10), 25);

        Pageable pageable = PageRequest.of(0, 10);
        CurrencyFilterDTO filters = new CurrencyFilterDTO();

        Mockito.when(currencyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(currencyPage);

        // Act
        Page<CurrencyDTO> obtained = currencyService.list(filters, pageable);

        // Assert
        assertEquals(25, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals(3, obtained.getTotalPages());
        assertEquals(0, obtained.getNumber());
    }

    @Test
    @DisplayName("List currencies with custom page size")
    public void testListWithCustomPageSize() {
        // Arrange
        Currency currency1 = new Currency();
        currency1.setId(1L);
        currency1.setName("Peso Argentino");
        currency1.setSymbol("$");
        currency1.setEnabled(true);

        List<Currency> currencies = Arrays.asList(currency1);
        Page<Currency> currencyPage = new PageImpl<>(currencies);

        Pageable pageable = PageRequest.of(0, 5);
        CurrencyFilterDTO filters = new CurrencyFilterDTO();

        Mockito.when(currencyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(currencyPage);

        // Act
        Page<CurrencyDTO> obtained = currencyService.list(filters, pageable);

        // Assert
        assertEquals(5, pageable.getPageSize());
        assertEquals(1, obtained.getContent().size());
    }

}