package com.spendwise.unittest;

import com.spendwise.dto.PaymentMethodDTO;
import com.spendwise.dto.PaymentMethodFilterDTO;
import com.spendwise.enums.PaymentMethodType;
import com.spendwise.model.PaymentMethod;
import com.spendwise.repository.PaymentMethodRepository;
import com.spendwise.service.PaymentMethodService;
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
@DisplayName("Payment Method Unit Tests")
public class PaymentMethodServiceTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentMethodService paymentMethodService;

    @Test
    @DisplayName("Create payment method saves a new paymentMethod successfully")
    public void testCreate() {

        // Arrange
        PaymentMethodDTO paymentMethodDTO = new PaymentMethodDTO();
        paymentMethodDTO.setName("Mercado Pago");
        paymentMethodDTO.setPaymentMethodType("DEBIT_CARD");
        paymentMethodDTO.setEnabled(true);

        PaymentMethodDTO expected = paymentMethodDTO;
        PaymentMethod paymentMethod = modelMapper.map(paymentMethodDTO, PaymentMethod.class);

        // Act
        Mockito.when(paymentMethodRepository.save(paymentMethod)).thenReturn(paymentMethod);
        PaymentMethodDTO obtained = paymentMethodService.create(paymentMethodDTO);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(paymentMethodRepository).save(paymentMethod);
        Mockito.verifyNoMoreInteractions(paymentMethodRepository);

    }

    @Test
    @DisplayName("Find payment method by ID returns the paymentMethod when it exists")
    public void testFindById() throws Exception {

        // Arrange
        Long id = 1L;

        PaymentMethodDTO paymentMethodDTO = new PaymentMethodDTO();
        paymentMethodDTO.setId(id);
        paymentMethodDTO.setName("Mercado Pago");
        paymentMethodDTO.setPaymentMethodType("DEBIT_CARD");
        paymentMethodDTO.setEnabled(true);

        PaymentMethodDTO expected = paymentMethodDTO;
        PaymentMethod paymentMethod = modelMapper.map(paymentMethodDTO, PaymentMethod.class);

        // Act
        Mockito.when(paymentMethodRepository.findById(id)).thenReturn(Optional.of(paymentMethod));
        PaymentMethodDTO obtained = paymentMethodService.findById(id);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(paymentMethodRepository).findById(id);
        Mockito.verifyNoMoreInteractions(paymentMethodRepository);
    }

    @Test
    @DisplayName("Find payment method by ID throws exception when paymentMethod does not exist")
    public void testFindNonExistingById() {

        // Arrange
        Long id = 1L;

        // Act & Assert
        Mockito.when(paymentMethodRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> paymentMethodService.findById(id));

    }

    @Test
    @DisplayName("List all payment methods returns complete list")
    public void testList() {
        // Arrange
        PaymentMethod paymentMethod1 = new PaymentMethod();
        paymentMethod1.setId(1L);
        paymentMethod1.setName("Mercado Pago");
        paymentMethod1.setPaymentMethodType(PaymentMethodType.DEBIT_CARD);
        paymentMethod1.setEnabled(true);

        PaymentMethod paymentMethod2 = new PaymentMethod();
        paymentMethod2.setId(2L);
        paymentMethod2.setName("Santander");
        paymentMethod2.setPaymentMethodType(PaymentMethodType.DEBIT_CARD);
        paymentMethod2.setEnabled(true);

        List<PaymentMethod> paymentMethods = Arrays.asList(paymentMethod1, paymentMethod2);
        Page<PaymentMethod> paymentMethodPage = new PageImpl<>(paymentMethods);

        Pageable pageable = PageRequest.of(0, 20);
        PaymentMethodFilterDTO filters = new PaymentMethodFilterDTO();

        // Act
        Mockito.when(paymentMethodRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentMethodPage);

        Page<PaymentMethodDTO> obtained = paymentMethodService.list(filters, pageable);

        // Assert
        assertEquals(2, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals("Mercado Pago", obtained.getContent().get(0).getName());
        assertEquals("Santander", obtained.getContent().get(1).getName());
        assertTrue(obtained.getContent().get(0).getEnabled());
        assertTrue(obtained.getContent().get(1).getEnabled());
    }

    @Test
    @DisplayName("List payment methods with name filter")
    public void testListWithNameFilter() {
        // Arrange
        PaymentMethod paymentMethod1 = new PaymentMethod();
        paymentMethod1.setId(1L);
        paymentMethod1.setName("Mercado Pago");
        paymentMethod1.setPaymentMethodType(PaymentMethodType.DEBIT_CARD);
        paymentMethod1.setEnabled(true);

        List<PaymentMethod> paymentMethods = Arrays.asList(paymentMethod1);
        Page<PaymentMethod> paymentMethodPage = new PageImpl<>(paymentMethods);

        Pageable pageable = PageRequest.of(0, 20);
        PaymentMethodFilterDTO filters = new PaymentMethodFilterDTO();
        filters.setName("Mercado");

        // Act
        Mockito.when(paymentMethodRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentMethodPage);

        Page<PaymentMethodDTO> obtained = paymentMethodService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Mercado Pago", obtained.getContent().get(0).getName());
    }

    @Test
    @DisplayName("List payment methods with payment method type filter")
    public void testListWithPaymentMethodTypeFilter() {
        // Arrange
        PaymentMethod paymentMethod1 = new PaymentMethod();
        paymentMethod1.setId(1L);
        paymentMethod1.setName("Mercado Pago");
        paymentMethod1.setPaymentMethodType(PaymentMethodType.DEBIT_CARD);
        paymentMethod1.setEnabled(true);

        PaymentMethod paymentMethod2 = new PaymentMethod();
        paymentMethod2.setId(2L);
        paymentMethod2.setName("Visa Credit");
        paymentMethod2.setPaymentMethodType(PaymentMethodType.CREDIT_CARD);
        paymentMethod2.setEnabled(true);

        List<PaymentMethod> paymentMethods = Arrays.asList(paymentMethod1);
        Page<PaymentMethod> paymentMethodPage = new PageImpl<>(paymentMethods);

        Pageable pageable = PageRequest.of(0, 20);
        PaymentMethodFilterDTO filters = new PaymentMethodFilterDTO();
        filters.setPaymentMethodType("DEBIT_CARD");

        // Act
        Mockito.when(paymentMethodRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentMethodPage);

        Page<PaymentMethodDTO> obtained = paymentMethodService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Mercado Pago", obtained.getContent().get(0).getName());
        assertEquals("DEBIT_CARD", obtained.getContent().get(0).getPaymentMethodType());
    }

    @Test
    @DisplayName("List payment methods with enabled filter true")
    public void testListWithEnabledFilterTrue() {
        // Arrange
        PaymentMethod paymentMethod1 = new PaymentMethod();
        paymentMethod1.setId(1L);
        paymentMethod1.setName("Mercado Pago");
        paymentMethod1.setPaymentMethodType(PaymentMethodType.DEBIT_CARD);
        paymentMethod1.setEnabled(true);

        List<PaymentMethod> paymentMethods = Arrays.asList(paymentMethod1);
        Page<PaymentMethod> paymentMethodPage = new PageImpl<>(paymentMethods);

        Pageable pageable = PageRequest.of(0, 20);
        PaymentMethodFilterDTO filters = new PaymentMethodFilterDTO();
        filters.setEnabled(true);

        // Act
        Mockito.when(paymentMethodRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentMethodPage);

        Page<PaymentMethodDTO> obtained = paymentMethodService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertTrue(obtained.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List payment methods with enabled filter false")
    public void testListWithEnabledFilterFalse() {
        // Arrange
        PaymentMethod paymentMethod1 = new PaymentMethod();
        paymentMethod1.setId(1L);
        paymentMethod1.setName("Old Card");
        paymentMethod1.setPaymentMethodType(PaymentMethodType.DEBIT_CARD);
        paymentMethod1.setEnabled(false);

        List<PaymentMethod> paymentMethods = Arrays.asList(paymentMethod1);
        Page<PaymentMethod> paymentMethodPage = new PageImpl<>(paymentMethods);

        Pageable pageable = PageRequest.of(0, 20);
        PaymentMethodFilterDTO filters = new PaymentMethodFilterDTO();
        filters.setEnabled(false);

        // Act
        Mockito.when(paymentMethodRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentMethodPage);

        Page<PaymentMethodDTO> obtained = paymentMethodService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertFalse(obtained.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List payment methods with multiple filters")
    public void testListWithMultipleFilters() {
        // Arrange
        PaymentMethod paymentMethod1 = new PaymentMethod();
        paymentMethod1.setId(1L);
        paymentMethod1.setName("Mercado Pago");
        paymentMethod1.setPaymentMethodType(PaymentMethodType.DEBIT_CARD);
        paymentMethod1.setEnabled(true);

        List<PaymentMethod> paymentMethods = Arrays.asList(paymentMethod1);
        Page<PaymentMethod> paymentMethodPage = new PageImpl<>(paymentMethods);

        Pageable pageable = PageRequest.of(0, 20);
        PaymentMethodFilterDTO filters = new PaymentMethodFilterDTO();
        filters.setName("Mercado");
        filters.setPaymentMethodType("DEBIT_CARD");
        filters.setEnabled(true);

        // Act
        Mockito.when(paymentMethodRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentMethodPage);

        Page<PaymentMethodDTO> obtained = paymentMethodService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Mercado Pago", obtained.getContent().get(0).getName());
        assertEquals("DEBIT_CARD", obtained.getContent().get(0).getPaymentMethodType());
        assertTrue(obtained.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List payment methods with no results")
    public void testListWithNoResults() {
        // Arrange
        List<PaymentMethod> paymentMethods = Collections.emptyList();
        Page<PaymentMethod> paymentMethodPage = new PageImpl<>(paymentMethods);

        Pageable pageable = PageRequest.of(0, 20);
        PaymentMethodFilterDTO filters = new PaymentMethodFilterDTO();
        filters.setName("NonExistent");

        // Act
        Mockito.when(paymentMethodRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentMethodPage);

        Page<PaymentMethodDTO> obtained = paymentMethodService.list(filters, pageable);

        // Assert
        assertEquals(0, obtained.getTotalElements());
        assertTrue(obtained.getContent().isEmpty());
    }

    @Test
    @DisplayName("List payment methods with pagination")
    public void testListWithPagination() {
        // Arrange
        PaymentMethod paymentMethod1 = new PaymentMethod();
        paymentMethod1.setId(1L);
        paymentMethod1.setName("Mercado Pago");
        paymentMethod1.setPaymentMethodType(PaymentMethodType.DEBIT_CARD);
        paymentMethod1.setEnabled(true);

        PaymentMethod paymentMethod2 = new PaymentMethod();
        paymentMethod2.setId(2L);
        paymentMethod2.setName("Santander");
        paymentMethod2.setPaymentMethodType(PaymentMethodType.DEBIT_CARD);
        paymentMethod2.setEnabled(true);

        List<PaymentMethod> paymentMethods = Arrays.asList(paymentMethod1, paymentMethod2);
        Page<PaymentMethod> paymentMethodPage = new PageImpl<>(paymentMethods, PageRequest.of(0, 10), 25);

        Pageable pageable = PageRequest.of(0, 10);
        PaymentMethodFilterDTO filters = new PaymentMethodFilterDTO();

        // Act
        Mockito.when(paymentMethodRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentMethodPage);

        Page<PaymentMethodDTO> obtained = paymentMethodService.list(filters, pageable);

        // Assert
        assertEquals(25, obtained.getTotalElements()); // total de registros
        assertEquals(2, obtained.getContent().size()); // registros en esta página
        assertEquals(3, obtained.getTotalPages()); // total de páginas (25/10 = 3)
        assertEquals(0, obtained.getNumber()); // página actual
    }

    @Test
    @DisplayName("List payment methods filtering by credit card type")
    public void testListFilteringByCreditCard() {
        // Arrange
        PaymentMethod paymentMethod1 = new PaymentMethod();
        paymentMethod1.setId(1L);
        paymentMethod1.setName("Visa Credit");
        paymentMethod1.setPaymentMethodType(PaymentMethodType.CREDIT_CARD);
        paymentMethod1.setEnabled(true);

        PaymentMethod paymentMethod2 = new PaymentMethod();
        paymentMethod2.setId(2L);
        paymentMethod2.setName("MasterCard");
        paymentMethod2.setPaymentMethodType(PaymentMethodType.CREDIT_CARD);
        paymentMethod2.setEnabled(true);

        List<PaymentMethod> paymentMethods = Arrays.asList(paymentMethod1, paymentMethod2);
        Page<PaymentMethod> paymentMethodPage = new PageImpl<>(paymentMethods);

        Pageable pageable = PageRequest.of(0, 20);
        PaymentMethodFilterDTO filters = new PaymentMethodFilterDTO();
        filters.setPaymentMethodType("CREDIT_CARD");

        // Act
        Mockito.when(paymentMethodRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentMethodPage);

        Page<PaymentMethodDTO> obtained = paymentMethodService.list(filters, pageable);

        // Assert
        assertEquals(2, obtained.getTotalElements());
        obtained.getContent().forEach(pm ->
                assertEquals("CREDIT_CARD", pm.getPaymentMethodType())
        );
    }

    @Test
    @DisplayName("Update payment method modifies existing paymentMethod successfully")
    public void testUpdate() throws Exception {

        // Arrange
        Long id = 1L;
        PaymentMethodDTO paymentMethodDTO = new PaymentMethodDTO();
        paymentMethodDTO.setId(id);
        paymentMethodDTO.setName("Mercado Pago");
        paymentMethodDTO.setPaymentMethodType("DEBIT_CARD");
        paymentMethodDTO.setEnabled(true);

        PaymentMethodDTO newPaymentMethodDTO = new PaymentMethodDTO();
        newPaymentMethodDTO.setId(id);
        newPaymentMethodDTO.setName("MERCADO PAGO");
        newPaymentMethodDTO.setPaymentMethodType("DEBIT_CARD");
        newPaymentMethodDTO.setEnabled(true);

        PaymentMethodDTO expected = newPaymentMethodDTO;
        PaymentMethod paymentMethod = modelMapper.map(paymentMethodDTO, PaymentMethod.class);

        Mockito.when(paymentMethodRepository.findById(id)).thenReturn(Optional.of(paymentMethod));
        Mockito.when(paymentMethodRepository.save(any(PaymentMethod.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PaymentMethodDTO obtained = paymentMethodService.update(id, newPaymentMethodDTO);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(paymentMethodRepository).findById(id);
        Mockito.verify(paymentMethodRepository).save(paymentMethod);
        Mockito.verifyNoMoreInteractions(paymentMethodRepository);

    }

    @Test
    @DisplayName("Delete payment method removes it from the database")
    public void testDelete() throws Exception {

        // Arrange
        Long id = 1L;
        PaymentMethodDTO paymentMethodDTO = new PaymentMethodDTO();
        paymentMethodDTO.setId(id);
        paymentMethodDTO.setName("Mercado Pago");
        paymentMethodDTO.setPaymentMethodType("DEBIT_CARD");
        paymentMethodDTO.setEnabled(true);

        PaymentMethodDTO expected = paymentMethodDTO;
        PaymentMethod paymentMethod = modelMapper.map(paymentMethodDTO, PaymentMethod.class);

        // Act
        Mockito.when(paymentMethodRepository.findById(id)).thenReturn(Optional.of(paymentMethod));
        PaymentMethodDTO deleted = paymentMethodService.delete(id);

        // Assert
        assertEquals(expected, deleted);
        Mockito.verify(paymentMethodRepository).findById(id);
        Mockito.verify(paymentMethodRepository).delete(paymentMethod);
        Mockito.verifyNoMoreInteractions(paymentMethodRepository);
    }

    @Test
    @DisplayName("Disable payment method sets enabled flag to false")
    public void testDisable() throws Exception {

        // Arrange
        Long id = 1L;
        PaymentMethodDTO paymentMethodDTO = new PaymentMethodDTO();
        paymentMethodDTO.setId(id);
        paymentMethodDTO.setName("Mercado Pago");
        paymentMethodDTO.setPaymentMethodType("DEBIT_CARD");
        paymentMethodDTO.setEnabled(true);
        PaymentMethod paymentMethod = modelMapper.map(paymentMethodDTO, PaymentMethod.class);

        Mockito.when(paymentMethodRepository.findById(id)).thenReturn(Optional.of(paymentMethod));
        Mockito.when(paymentMethodRepository.save(any(PaymentMethod.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PaymentMethodDTO obtained = paymentMethodService.disable(id);

        // Assert
        assertFalse(obtained.getEnabled());
        Mockito.verify(paymentMethodRepository).findById(id);
        Mockito.verify(paymentMethodRepository).save(paymentMethod);
        Mockito.verifyNoMoreInteractions(paymentMethodRepository);

    }

    @Test
    @DisplayName("Enable payment method sets enabled flag to true")
    public void testEnable() throws Exception {

        // Arrange
        Long id = 1L;
        PaymentMethodDTO paymentMethodDTO = new PaymentMethodDTO();
        paymentMethodDTO.setId(id);
        paymentMethodDTO.setName("Mercado Pago");
        paymentMethodDTO.setPaymentMethodType("DEBIT_CARD");
        paymentMethodDTO.setEnabled(false);
        PaymentMethod paymentMethod = modelMapper.map(paymentMethodDTO, PaymentMethod.class);

        Mockito.when(paymentMethodRepository.findById(id)).thenReturn(Optional.of(paymentMethod));
        Mockito.when(paymentMethodRepository.save(any(PaymentMethod.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PaymentMethodDTO obtained = paymentMethodService.enable(id);

        // Assert
        assertTrue(obtained.getEnabled());
        Mockito.verify(paymentMethodRepository).findById(id);
        Mockito.verify(paymentMethodRepository).save(paymentMethod);
        Mockito.verifyNoMoreInteractions(paymentMethodRepository);

    }

}
