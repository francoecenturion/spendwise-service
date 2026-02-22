package com.spendwise.service.interfaces;

import com.spendwise.dto.SavingsWalletDTO;
import com.spendwise.dto.SavingsWalletFilterDTO;
import com.spendwise.model.SavingsWallet;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISavingsWalletService {

    void populate(SavingsWallet savingsWallet, SavingsWalletDTO dto);
    SavingsWalletDTO create(SavingsWalletDTO dto);
    SavingsWalletDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<SavingsWalletDTO> list(SavingsWalletFilterDTO filters, Pageable pageable);
    SavingsWalletDTO update(Long id, SavingsWalletDTO dto) throws ChangeSetPersister.NotFoundException;
    SavingsWalletDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    SavingsWalletDTO disable(Long id) throws ChangeSetPersister.NotFoundException;
    SavingsWalletDTO enable(Long id) throws ChangeSetPersister.NotFoundException;
}
