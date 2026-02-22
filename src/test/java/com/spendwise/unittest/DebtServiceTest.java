package com.spendwise.unittest;

import com.spendwise.dto.DebtDTO;
import com.spendwise.dto.DebtFilterDTO;
import com.spendwise.dto.IssuingEntityDTO;
import com.spendwise.dto.PaymentMethodDTO;
import com.spendwise.enums.PaymentMethodType;
import com.spendwise.model.Debt;
import com.spendwise.model.IssuingEntity;
import com.spendwise.model.PaymentMethod;
import com.spendwise.repository.DebtRepository;
import com.spendwise.service.DebtService;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("Debt Unit Tests")
public class DebtServiceTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private DebtRepository debtRepository;

    @InjectMocks
    private DebtService debtService;

    private static IssuingEntity issuingEntity;
    private static PaymentMethod paymentMethod;

    @BeforeAll
    public static void init() {
        issuingEntity = new IssuingEntity();
        issuingEntity.setId(1L);
        issuingEntity.setDescription("Banco Nación");
        issuingEntity.setEnabled(true);

        paymentMethod = new PaymentMethod();
        paymentMethod.setId(1L);
        paymentMethod.setName("Visa Nación");
        paymentMethod.setPaymentMethodType(PaymentMethodType.CREDIT_CARD);
        paymentMethod.setEnabled(true);
    }

    @Test
    @DisplayName("Create personal debt saves with creditor name and no issuing entity")
    public void testCreatePersonalDebt() {
        // Arrange
        DebtDTO dto = new DebtDTO();
        dto.setDescription("Deuda con Juan");
        dto.setAmountInPesos(BigDecimal.valueOf(5000));
        dto.setDate(LocalDate.now());
        dto.setPersonal(true);
        dto.setCreditor("Juan Pérez");

        Debt saved = new Debt();
        saved.setId(1L);
        saved.setDescription("Deuda con Juan");
        saved.setAmountInPesos(BigDecimal.valueOf(5000));
        saved.setDate(LocalDate.now());
        saved.setPersonal(true);
        saved.setCreditor("Juan Pérez");
        saved.setCancelled(false);

        // Act
        Mockito.when(debtRepository.save(any(Debt.class))).thenReturn(saved);
        DebtDTO result = debtService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Deuda con Juan", result.getDescription());
        assertTrue(result.getPersonal());
        assertEquals("Juan Pérez", result.getCreditor());
        assertFalse(result.getCancelled());
        assertNull(result.getIssuingEntity());
        Mockito.verify(debtRepository).save(any(Debt.class));
    }

    @Test
    @DisplayName("Create institutional debt saves with issuing entity and no creditor")
    public void testCreateInstitutionalDebt() {
        // Arrange
        DebtDTO dto = new DebtDTO();
        dto.setDescription("Tarjeta Visa");
        dto.setAmountInPesos(BigDecimal.valueOf(20000));
        dto.setDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusMonths(1));
        dto.setPersonal(false);
        dto.setIssuingEntity(modelMapper.map(issuingEntity, IssuingEntityDTO.class));
        dto.setPaymentMethod(modelMapper.map(paymentMethod, PaymentMethodDTO.class));

        Debt saved = new Debt();
        saved.setId(2L);
        saved.setDescription("Tarjeta Visa");
        saved.setAmountInPesos(BigDecimal.valueOf(20000));
        saved.setDate(LocalDate.now());
        saved.setDueDate(LocalDate.now().plusMonths(1));
        saved.setPersonal(false);
        saved.setIssuingEntity(issuingEntity);
        saved.setPaymentMethod(paymentMethod);
        saved.setCancelled(false);

        // Act
        Mockito.when(debtRepository.save(any(Debt.class))).thenReturn(saved);
        DebtDTO result = debtService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Tarjeta Visa", result.getDescription());
        assertFalse(result.getPersonal());
        assertFalse(result.getCancelled());
        assertNotNull(result.getIssuingEntity());
        assertEquals("Banco Nación", result.getIssuingEntity().getDescription());
        assertEquals(1L, result.getPaymentMethod().getId());
        Mockito.verify(debtRepository).save(any(Debt.class));
    }

    @Test
    @DisplayName("Find debt by ID returns the debt when it exists")
    public void testFindById() throws Exception {
        // Arrange
        Long id = 1L;

        Debt debt = new Debt();
        debt.setId(id);
        debt.setDescription("Deuda con Juan");
        debt.setAmountInPesos(BigDecimal.valueOf(5000));
        debt.setDate(LocalDate.now());
        debt.setPersonal(true);
        debt.setCreditor("Juan Pérez");
        debt.setCancelled(false);

        // Act
        Mockito.when(debtRepository.findById(id)).thenReturn(Optional.of(debt));
        DebtDTO result = debtService.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Deuda con Juan", result.getDescription());
        Mockito.verify(debtRepository).findById(id);
        Mockito.verifyNoMoreInteractions(debtRepository);
    }

    @Test
    @DisplayName("Find debt by ID throws exception when debt does not exist")
    public void testFindNonExistingById() {
        // Arrange
        Long id = 999L;

        // Act & Assert
        Mockito.when(debtRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> debtService.findById(id));
        Mockito.verify(debtRepository).findById(id);
    }

    @Test
    @DisplayName("List all debts returns complete list")
    public void testList() {
        // Arrange
        Debt debt1 = new Debt();
        debt1.setId(1L);
        debt1.setDescription("Deuda con Juan");
        debt1.setAmountInPesos(BigDecimal.valueOf(5000));
        debt1.setDate(LocalDate.now());
        debt1.setPersonal(true);
        debt1.setCreditor("Juan Pérez");
        debt1.setCancelled(false);

        Debt debt2 = new Debt();
        debt2.setId(2L);
        debt2.setDescription("Tarjeta Visa");
        debt2.setAmountInPesos(BigDecimal.valueOf(20000));
        debt2.setDate(LocalDate.now());
        debt2.setPersonal(false);
        debt2.setIssuingEntity(issuingEntity);
        debt2.setPaymentMethod(paymentMethod);
        debt2.setCancelled(false);

        List<Debt> debts = Arrays.asList(debt1, debt2);
        Page<Debt> debtPage = new PageImpl<>(debts);

        Pageable pageable = PageRequest.of(0, 20);
        DebtFilterDTO filters = new DebtFilterDTO();

        // Act
        Mockito.when(debtRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(debtPage);

        Page<DebtDTO> result = debtService.list(filters, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals("Deuda con Juan", result.getContent().get(0).getDescription());
        assertEquals("Tarjeta Visa", result.getContent().get(1).getDescription());
    }

    @Test
    @DisplayName("List debts with description filter")
    public void testListWithDescriptionFilter() {
        // Arrange
        Debt debt1 = new Debt();
        debt1.setId(1L);
        debt1.setDescription("Deuda con Juan");
        debt1.setAmountInPesos(BigDecimal.valueOf(5000));
        debt1.setDate(LocalDate.now());
        debt1.setPersonal(true);
        debt1.setCreditor("Juan Pérez");
        debt1.setCancelled(false);

        List<Debt> debts = Arrays.asList(debt1);
        Page<Debt> debtPage = new PageImpl<>(debts);

        Pageable pageable = PageRequest.of(0, 20);
        DebtFilterDTO filters = new DebtFilterDTO();
        filters.setDescription("Juan");

        // Act
        Mockito.when(debtRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(debtPage);

        Page<DebtDTO> result = debtService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getDescription().contains("Juan"));
    }

    @Test
    @DisplayName("List debts with cancelled filter")
    public void testListWithCancelledFilter() {
        // Arrange
        Debt debt1 = new Debt();
        debt1.setId(1L);
        debt1.setDescription("Deuda cancelada");
        debt1.setAmountInPesos(BigDecimal.valueOf(3000));
        debt1.setDate(LocalDate.now().minusMonths(1));
        debt1.setPersonal(true);
        debt1.setCreditor("María García");
        debt1.setCancelled(true);

        List<Debt> debts = Arrays.asList(debt1);
        Page<Debt> debtPage = new PageImpl<>(debts);

        Pageable pageable = PageRequest.of(0, 20);
        DebtFilterDTO filters = new DebtFilterDTO();
        filters.setCancelled(true);

        // Act
        Mockito.when(debtRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(debtPage);

        Page<DebtDTO> result = debtService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getCancelled());
    }

    @Test
    @DisplayName("List debts filtering only personal debts")
    public void testListWithPersonalFilter() {
        // Arrange
        Debt debt1 = new Debt();
        debt1.setId(1L);
        debt1.setDescription("Deuda con Juan");
        debt1.setAmountInPesos(BigDecimal.valueOf(5000));
        debt1.setDate(LocalDate.now());
        debt1.setPersonal(true);
        debt1.setCreditor("Juan Pérez");
        debt1.setCancelled(false);

        List<Debt> debts = Arrays.asList(debt1);
        Page<Debt> debtPage = new PageImpl<>(debts);

        Pageable pageable = PageRequest.of(0, 20);
        DebtFilterDTO filters = new DebtFilterDTO();
        filters.setPersonal(true);

        // Act
        Mockito.when(debtRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(debtPage);

        Page<DebtDTO> result = debtService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getPersonal());
        assertEquals("Juan Pérez", result.getContent().get(0).getCreditor());
    }

    @Test
    @DisplayName("List debts with date range filter")
    public void testListWithDateRangeFilter() {
        // Arrange
        LocalDate today = LocalDate.now();

        Debt debt1 = new Debt();
        debt1.setId(1L);
        debt1.setDescription("Deuda reciente");
        debt1.setAmountInPesos(BigDecimal.valueOf(5000));
        debt1.setDate(today);
        debt1.setPersonal(true);
        debt1.setCreditor("Juan");
        debt1.setCancelled(false);

        List<Debt> debts = Arrays.asList(debt1);
        Page<Debt> debtPage = new PageImpl<>(debts);

        Pageable pageable = PageRequest.of(0, 20);
        DebtFilterDTO filters = new DebtFilterDTO();
        filters.setStartDate(today.minusDays(7));
        filters.setEndDate(today.plusDays(1));

        // Act
        Mockito.when(debtRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(debtPage);

        Page<DebtDTO> result = debtService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertNotNull(result.getContent().get(0).getDate());
    }

    @Test
    @DisplayName("List debts with no results")
    public void testListWithNoResults() {
        // Arrange
        List<Debt> debts = Collections.emptyList();
        Page<Debt> debtPage = new PageImpl<>(debts);

        Pageable pageable = PageRequest.of(0, 20);
        DebtFilterDTO filters = new DebtFilterDTO();
        filters.setDescription("NoExiste");

        // Act
        Mockito.when(debtRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(debtPage);

        Page<DebtDTO> result = debtService.list(filters, pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("List debts with pagination")
    public void testListWithPagination() {
        // Arrange
        Debt debt1 = new Debt();
        debt1.setId(1L);
        debt1.setDescription("Deuda 1");
        debt1.setAmountInPesos(BigDecimal.valueOf(1000));
        debt1.setDate(LocalDate.now());
        debt1.setPersonal(true);
        debt1.setCreditor("Juan");
        debt1.setCancelled(false);

        Debt debt2 = new Debt();
        debt2.setId(2L);
        debt2.setDescription("Deuda 2");
        debt2.setAmountInPesos(BigDecimal.valueOf(2000));
        debt2.setDate(LocalDate.now());
        debt2.setPersonal(false);
        debt2.setIssuingEntity(issuingEntity);
        debt2.setCancelled(false);

        List<Debt> debts = Arrays.asList(debt1, debt2);
        Page<Debt> debtPage = new PageImpl<>(debts, PageRequest.of(0, 10), 25);

        Pageable pageable = PageRequest.of(0, 10);
        DebtFilterDTO filters = new DebtFilterDTO();

        // Act
        Mockito.when(debtRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(debtPage);

        Page<DebtDTO> result = debtService.list(filters, pageable);

        // Assert
        assertEquals(25, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalPages());
        assertEquals(0, result.getNumber());
    }

    @Test
    @DisplayName("Update debt modifies existing debt successfully")
    public void testUpdate() throws Exception {
        // Arrange
        Long id = 1L;

        Debt existing = new Debt();
        existing.setId(id);
        existing.setDescription("Deuda original");
        existing.setAmountInPesos(BigDecimal.valueOf(5000));
        existing.setDate(LocalDate.now());
        existing.setPersonal(true);
        existing.setCreditor("Juan");
        existing.setCancelled(false);

        DebtDTO updateDTO = new DebtDTO();
        updateDTO.setId(id);
        updateDTO.setDescription("Deuda actualizada");
        updateDTO.setAmountInPesos(BigDecimal.valueOf(6000));
        updateDTO.setDate(LocalDate.now());
        updateDTO.setPersonal(true);
        updateDTO.setCreditor("Juan Pérez");

        // Act
        Mockito.when(debtRepository.findById(id)).thenReturn(Optional.of(existing));
        Mockito.when(debtRepository.save(any(Debt.class))).thenAnswer(inv -> inv.getArgument(0));

        DebtDTO result = debtService.update(id, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Deuda actualizada", result.getDescription());
        assertEquals(BigDecimal.valueOf(6000), result.getAmountInPesos());
        Mockito.verify(debtRepository).findById(id);
        Mockito.verify(debtRepository).save(any(Debt.class));
        Mockito.verifyNoMoreInteractions(debtRepository);
    }

    @Test
    @DisplayName("Delete debt removes it from the database")
    public void testDelete() throws Exception {
        // Arrange
        Long id = 1L;

        Debt debt = new Debt();
        debt.setId(id);
        debt.setDescription("Deuda a eliminar");
        debt.setAmountInPesos(BigDecimal.valueOf(5000));
        debt.setDate(LocalDate.now());
        debt.setPersonal(true);
        debt.setCreditor("Juan");
        debt.setCancelled(false);

        // Act
        Mockito.when(debtRepository.findById(id)).thenReturn(Optional.of(debt));
        DebtDTO result = debtService.delete(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        Mockito.verify(debtRepository).findById(id);
        Mockito.verify(debtRepository).delete(debt);
        Mockito.verifyNoMoreInteractions(debtRepository);
    }

    @Test
    @DisplayName("Cancel debt sets cancelled flag to true")
    public void testCancel() throws Exception {
        // Arrange
        Long id = 1L;

        Debt debt = new Debt();
        debt.setId(id);
        debt.setDescription("Deuda activa");
        debt.setAmountInPesos(BigDecimal.valueOf(5000));
        debt.setDate(LocalDate.now());
        debt.setPersonal(true);
        debt.setCreditor("Juan");
        debt.setCancelled(false);

        Mockito.when(debtRepository.findById(id)).thenReturn(Optional.of(debt));
        Mockito.when(debtRepository.save(any(Debt.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DebtDTO result = debtService.cancel(id);

        // Assert
        assertTrue(result.getCancelled());
        Mockito.verify(debtRepository).findById(id);
        Mockito.verify(debtRepository).save(debt);
        Mockito.verifyNoMoreInteractions(debtRepository);
    }

    @Test
    @DisplayName("Uncancel debt sets cancelled flag to false")
    public void testUncancel() throws Exception {
        // Arrange
        Long id = 1L;

        Debt debt = new Debt();
        debt.setId(id);
        debt.setDescription("Deuda cancelada");
        debt.setAmountInPesos(BigDecimal.valueOf(5000));
        debt.setDate(LocalDate.now());
        debt.setPersonal(true);
        debt.setCreditor("Juan");
        debt.setCancelled(true);

        Mockito.when(debtRepository.findById(id)).thenReturn(Optional.of(debt));
        Mockito.when(debtRepository.save(any(Debt.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DebtDTO result = debtService.uncancel(id);

        // Assert
        assertFalse(result.getCancelled());
        Mockito.verify(debtRepository).findById(id);
        Mockito.verify(debtRepository).save(debt);
        Mockito.verifyNoMoreInteractions(debtRepository);
    }

}
