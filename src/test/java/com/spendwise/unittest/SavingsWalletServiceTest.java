package com.spendwise.unittest;

import com.spendwise.dto.SavingsWalletDTO;
import com.spendwise.dto.SavingsWalletFilterDTO;
import com.spendwise.enums.SavingsWalletType;
import com.spendwise.model.SavingsWallet;
import com.spendwise.repository.SavingsWalletRepository;
import com.spendwise.service.SavingsWalletService;
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
@DisplayName("SavingsWallet Unit Tests")
public class SavingsWalletServiceTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private SavingsWalletRepository savingsWalletRepository;

    @InjectMocks
    private SavingsWalletService savingsWalletService;

    // ──────────────────────────────────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Create savings wallet saves a new wallet successfully and sets enabled=true")
    public void testCreate() {
        // Arrange
        SavingsWalletDTO dto = new SavingsWalletDTO();
        dto.setName("Cuenta Galicia");
        dto.setSavingsWalletType("BANK_ACCOUNT");
        dto.setIcon("🏦");

        SavingsWallet wallet = new SavingsWallet();
        wallet.setName("Cuenta Galicia");
        wallet.setSavingsWalletType(SavingsWalletType.BANK_ACCOUNT);
        wallet.setIcon("🏦");
        wallet.setEnabled(true);

        Mockito.when(savingsWalletRepository.save(any(SavingsWallet.class))).thenReturn(wallet);

        // Act
        SavingsWalletDTO result = savingsWalletService.create(dto);

        // Assert
        assertEquals("Cuenta Galicia", result.getName());
        assertEquals("BANK_ACCOUNT", result.getSavingsWalletType());
        assertTrue(result.getEnabled());
        Mockito.verify(savingsWalletRepository).save(any(SavingsWallet.class));
        Mockito.verifyNoMoreInteractions(savingsWalletRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FIND BY ID
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Find savings wallet by ID returns the wallet when it exists")
    public void testFindById() throws Exception {
        // Arrange
        Long id = 1L;
        SavingsWallet wallet = new SavingsWallet();
        wallet.setId(id);
        wallet.setName("Mercado Pago");;
        wallet.setSavingsWalletType(SavingsWalletType.VIRTUAL_WALLET);
        wallet.setEnabled(true);

        Mockito.when(savingsWalletRepository.findById(id)).thenReturn(Optional.of(wallet));

        // Act
        SavingsWalletDTO result = savingsWalletService.findById(id);

        // Assert
        assertEquals(id, result.getId());
        assertEquals("Mercado Pago", result.getName());
        Mockito.verify(savingsWalletRepository).findById(id);
        Mockito.verifyNoMoreInteractions(savingsWalletRepository);
    }

    @Test
    @DisplayName("Find savings wallet by ID throws NotFoundException when wallet does not exist")
    public void testFindByIdNotFound() {
        // Arrange
        Long id = 99L;
        Mockito.when(savingsWalletRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> savingsWalletService.findById(id));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LIST
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("List all savings wallets returns complete list")
    public void testList() {
        // Arrange
        SavingsWallet wallet1 = new SavingsWallet();
        wallet1.setId(1L);
        wallet1.setName("Cuenta Galicia");
        wallet1.setSavingsWalletType(SavingsWalletType.BANK_ACCOUNT);
        wallet1.setEnabled(true);

        SavingsWallet wallet2 = new SavingsWallet();
        wallet2.setId(2L);
        wallet2.setName("Mercado Pago");
        wallet2.setSavingsWalletType(SavingsWalletType.VIRTUAL_WALLET);
        wallet2.setEnabled(true);

        Page<SavingsWallet> page = new PageImpl<>(Arrays.asList(wallet1, wallet2));
        Pageable pageable = PageRequest.of(0, 20);
        SavingsWalletFilterDTO filters = new SavingsWalletFilterDTO();

        Mockito.when(savingsWalletRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingsWalletDTO> result = savingsWalletService.list(filters, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals("Cuenta Galicia", result.getContent().get(0).getName());
        assertEquals("Mercado Pago", result.getContent().get(1).getName());
    }

    @Test
    @DisplayName("List savings wallets filtered by name")
    public void testListWithNameFilter() {
        // Arrange
        SavingsWallet wallet = new SavingsWallet();
        wallet.setId(1L);
        wallet.setName("Cuenta Galicia");
        wallet.setSavingsWalletType(SavingsWalletType.BANK_ACCOUNT);
        wallet.setEnabled(true);

        Page<SavingsWallet> page = new PageImpl<>(List.of(wallet));
        Pageable pageable = PageRequest.of(0, 20);
        SavingsWalletFilterDTO filters = new SavingsWalletFilterDTO();
        filters.setName("Galicia");

        Mockito.when(savingsWalletRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingsWalletDTO> result = savingsWalletService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Cuenta Galicia", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("List savings wallets filtered by wallet type")
    public void testListWithWalletTypeFilter() {
        // Arrange
        SavingsWallet wallet = new SavingsWallet();
        wallet.setId(1L);
        wallet.setName("PPI");
        wallet.setSavingsWalletType(SavingsWalletType.MUTUAL_FUND);
        wallet.setEnabled(true);

        Page<SavingsWallet> page = new PageImpl<>(List.of(wallet));
        Pageable pageable = PageRequest.of(0, 20);
        SavingsWalletFilterDTO filters = new SavingsWalletFilterDTO();
        filters.setSavingsWalletType("MUTUAL_FUND");

        Mockito.when(savingsWalletRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingsWalletDTO> result = savingsWalletService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("MUTUAL_FUND", result.getContent().get(0).getSavingsWalletType());
    }

    @Test
    @DisplayName("List savings wallets filtered by enabled=true")
    public void testListWithEnabledFilterTrue() {
        // Arrange
        SavingsWallet wallet = new SavingsWallet();
        wallet.setId(1L);
        wallet.setName("Cuenta Santander");
        wallet.setSavingsWalletType(SavingsWalletType.BANK_ACCOUNT);
        wallet.setEnabled(true);

        Page<SavingsWallet> page = new PageImpl<>(List.of(wallet));
        Pageable pageable = PageRequest.of(0, 20);
        SavingsWalletFilterDTO filters = new SavingsWalletFilterDTO();
        filters.setEnabled(true);

        Mockito.when(savingsWalletRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingsWalletDTO> result = savingsWalletService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List savings wallets filtered by enabled=false")
    public void testListWithEnabledFilterFalse() {
        // Arrange
        SavingsWallet wallet = new SavingsWallet();
        wallet.setId(1L);
        wallet.setName("Plazo Fijo Vencido");
        wallet.setSavingsWalletType(SavingsWalletType.FIXED_TERM);
        wallet.setEnabled(false);

        Page<SavingsWallet> page = new PageImpl<>(List.of(wallet));
        Pageable pageable = PageRequest.of(0, 20);
        SavingsWalletFilterDTO filters = new SavingsWalletFilterDTO();
        filters.setEnabled(false);

        Mockito.when(savingsWalletRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingsWalletDTO> result = savingsWalletService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertFalse(result.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List savings wallets with multiple filters combined")
    public void testListWithMultipleFilters() {
        // Arrange
        SavingsWallet wallet = new SavingsWallet();
        wallet.setId(1L);
        wallet.setName("Efectivo Casa");
        wallet.setSavingsWalletType(SavingsWalletType.CASH);
        wallet.setEnabled(true);

        Page<SavingsWallet> page = new PageImpl<>(List.of(wallet));
        Pageable pageable = PageRequest.of(0, 20);
        SavingsWalletFilterDTO filters = new SavingsWalletFilterDTO();
        filters.setName("Efectivo");
        filters.setSavingsWalletType("CASH");
        filters.setEnabled(true);

        Mockito.when(savingsWalletRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingsWalletDTO> result = savingsWalletService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Efectivo Casa", result.getContent().get(0).getName());
        assertEquals("CASH", result.getContent().get(0).getSavingsWalletType());
        assertTrue(result.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List savings wallets returns empty page when no results match")
    public void testListNoResults() {
        // Arrange
        Page<SavingsWallet> page = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 20);
        SavingsWalletFilterDTO filters = new SavingsWalletFilterDTO();
        filters.setName("Inexistente");

        Mockito.when(savingsWalletRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingsWalletDTO> result = savingsWalletService.list(filters, pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("List savings wallets with pagination metadata is correct")
    public void testListWithPagination() {
        // Arrange
        SavingsWallet wallet1 = new SavingsWallet();
        wallet1.setId(1L);
        wallet1.setName("Cuenta A");
        wallet1.setSavingsWalletType(SavingsWalletType.BANK_ACCOUNT);
        wallet1.setEnabled(true);

        SavingsWallet wallet2 = new SavingsWallet();
        wallet2.setId(2L);
        wallet2.setName("Cuenta B");
        wallet2.setSavingsWalletType(SavingsWalletType.BANK_ACCOUNT);
        wallet2.setEnabled(true);

        Pageable pageable = PageRequest.of(0, 2);
        Page<SavingsWallet> page = new PageImpl<>(Arrays.asList(wallet1, wallet2), pageable, 5);
        SavingsWalletFilterDTO filters = new SavingsWalletFilterDTO();

        Mockito.when(savingsWalletRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<SavingsWalletDTO> result = savingsWalletService.list(filters, pageable);

        // Assert
        assertEquals(5, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalPages());
        assertEquals(0, result.getNumber());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Update savings wallet modifies name and type correctly")
    public void testUpdate() throws Exception {
        // Arrange
        Long id = 1L;

        SavingsWallet existing = new SavingsWallet();
        existing.setId(id);
        existing.setName("Cuenta Galicia");
        existing.setSavingsWalletType(SavingsWalletType.BANK_ACCOUNT);
        existing.setEnabled(true);

        SavingsWalletDTO updateDTO = new SavingsWalletDTO();
        updateDTO.setId(id);
        updateDTO.setName("Cuenta Galicia Dólares");
        updateDTO.setSavingsWalletType("BANK_ACCOUNT");
        updateDTO.setEnabled(true);

        Mockito.when(savingsWalletRepository.findById(id)).thenReturn(Optional.of(existing));
        Mockito.when(savingsWalletRepository.save(any(SavingsWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SavingsWalletDTO result = savingsWalletService.update(id, updateDTO);

        // Assert
        assertEquals("Cuenta Galicia Dólares", result.getName());
        Mockito.verify(savingsWalletRepository).findById(id);
        Mockito.verify(savingsWalletRepository).save(existing);
        Mockito.verifyNoMoreInteractions(savingsWalletRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Delete savings wallet removes it from the repository")
    public void testDelete() throws Exception {
        // Arrange
        Long id = 1L;
        SavingsWallet wallet = new SavingsWallet();
        wallet.setId(id);
        wallet.setName("Cuenta a Eliminar");
        wallet.setSavingsWalletType(SavingsWalletType.CASH);
        wallet.setEnabled(true);

        Mockito.when(savingsWalletRepository.findById(id)).thenReturn(Optional.of(wallet));

        // Act
        SavingsWalletDTO result = savingsWalletService.delete(id);

        // Assert
        assertEquals("Cuenta a Eliminar", result.getName());
        Mockito.verify(savingsWalletRepository).findById(id);
        Mockito.verify(savingsWalletRepository).delete(wallet);
        Mockito.verifyNoMoreInteractions(savingsWalletRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ENABLE / DISABLE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Disable savings wallet sets enabled to false")
    public void testDisable() throws Exception {
        // Arrange
        Long id = 1L;
        SavingsWallet wallet = new SavingsWallet();
        wallet.setId(id);
        wallet.setName("Plazo Fijo");
        wallet.setSavingsWalletType(SavingsWalletType.FIXED_TERM);
        wallet.setEnabled(true);

        Mockito.when(savingsWalletRepository.findById(id)).thenReturn(Optional.of(wallet));
        Mockito.when(savingsWalletRepository.save(any(SavingsWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SavingsWalletDTO result = savingsWalletService.disable(id);

        // Assert
        assertFalse(result.getEnabled());
        Mockito.verify(savingsWalletRepository).findById(id);
        Mockito.verify(savingsWalletRepository).save(wallet);
        Mockito.verifyNoMoreInteractions(savingsWalletRepository);
    }

    @Test
    @DisplayName("Enable savings wallet sets enabled to true")
    public void testEnable() throws Exception {
        // Arrange
        Long id = 1L;
        SavingsWallet wallet = new SavingsWallet();
        wallet.setId(id);
        wallet.setName("Plazo Fijo");
        wallet.setSavingsWalletType(SavingsWalletType.FIXED_TERM);
        wallet.setEnabled(false);

        Mockito.when(savingsWalletRepository.findById(id)).thenReturn(Optional.of(wallet));
        Mockito.when(savingsWalletRepository.save(any(SavingsWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SavingsWalletDTO result = savingsWalletService.enable(id);

        // Assert
        assertTrue(result.getEnabled());
        Mockito.verify(savingsWalletRepository).findById(id);
        Mockito.verify(savingsWalletRepository).save(wallet);
        Mockito.verifyNoMoreInteractions(savingsWalletRepository);
    }
}
