package com.spendwise.service;

import com.spendwise.dto.SavingsWalletDTO;
import com.spendwise.dto.SavingsWalletFilterDTO;
import com.spendwise.enums.SavingsWalletType;
import com.spendwise.model.SavingsWallet;
import com.spendwise.repository.SavingsWalletRepository;
import com.spendwise.service.interfaces.ISavingsWalletService;
import com.spendwise.spec.SavingsWalletEspecification;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SavingsWalletService implements ISavingsWalletService {

    private static final Logger log = LoggerFactory.getLogger(SavingsWalletService.class);

    private final SavingsWalletRepository savingsWalletRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public SavingsWalletService(SavingsWalletRepository savingsWalletRepository) {
        this.savingsWalletRepository = savingsWalletRepository;
    }

    @Override
    public void populate(SavingsWallet savingsWallet, SavingsWalletDTO dto) {
        savingsWallet.setName(dto.getName());
        savingsWallet.setSavingsWalletType(SavingsWalletType.valueOf(dto.getSavingsWalletType()));
        savingsWallet.setIcon(dto.getIcon());
    }

    @Transactional
    @Override
    public SavingsWalletDTO create(SavingsWalletDTO dto) {
        SavingsWallet savingsWallet = new SavingsWallet();
        this.populate(savingsWallet, dto);
        savingsWallet.setEnabled(true);
        SavingsWallet saved = savingsWalletRepository.save(savingsWallet);
        log.debug("SavingsWallet with id {} created successfully", saved.getId());
        return modelMapper.map(saved, SavingsWalletDTO.class);
    }

    @Transactional
    @Override
    public SavingsWalletDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        SavingsWallet savingsWallet = find(id);
        log.debug("SavingsWallet with id {} read successfully", savingsWallet.getId());
        return modelMapper.map(savingsWallet, SavingsWalletDTO.class);
    }

    @Override
    public Page<SavingsWalletDTO> list(SavingsWalletFilterDTO filters, Pageable pageable) {
        log.debug("Listing all savings wallets");
        Specification<SavingsWallet> spec = SavingsWalletEspecification.withFilters(filters);
        return savingsWalletRepository.findAll(spec, pageable)
                .map(savingsWallet -> modelMapper.map(savingsWallet, SavingsWalletDTO.class));
    }

    @Transactional
    @Override
    public SavingsWalletDTO update(Long id, SavingsWalletDTO dto) throws ChangeSetPersister.NotFoundException {
        SavingsWallet savingsWallet = find(id);
        this.populate(savingsWallet, dto);
        SavingsWallet updated = savingsWalletRepository.save(savingsWallet);
        log.debug("SavingsWallet with id {} updated successfully", savingsWallet.getId());
        return modelMapper.map(updated, SavingsWalletDTO.class);
    }

    @Transactional
    @Override
    public SavingsWalletDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        SavingsWallet savingsWallet = find(id);
        savingsWalletRepository.delete(savingsWallet);
        log.debug("SavingsWallet with id {} deleted successfully", savingsWallet.getId());
        return modelMapper.map(savingsWallet, SavingsWalletDTO.class);
    }

    @Transactional
    @Override
    public SavingsWalletDTO disable(Long id) throws ChangeSetPersister.NotFoundException {
        SavingsWallet savingsWallet = find(id);
        savingsWallet.setEnabled(false);
        SavingsWallet saved = savingsWalletRepository.save(savingsWallet);
        log.debug("SavingsWallet with id {} disabled successfully", savingsWallet.getId());
        return modelMapper.map(saved, SavingsWalletDTO.class);
    }

    @Transactional
    @Override
    public SavingsWalletDTO enable(Long id) throws ChangeSetPersister.NotFoundException {
        SavingsWallet savingsWallet = find(id);
        savingsWallet.setEnabled(true);
        SavingsWallet saved = savingsWalletRepository.save(savingsWallet);
        log.debug("SavingsWallet with id {} enabled successfully", savingsWallet.getId());
        return modelMapper.map(saved, SavingsWalletDTO.class);
    }

    protected SavingsWallet find(Long id) throws ChangeSetPersister.NotFoundException {
        return savingsWalletRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }
}
