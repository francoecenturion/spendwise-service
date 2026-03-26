package com.spendwise.service;

import com.spendwise.dto.SavingDTO;
import com.spendwise.dto.SavingFilterDTO;
import com.spendwise.model.Currency;
import com.spendwise.model.Saving;
import com.spendwise.model.SavingsWallet;
import com.spendwise.repository.SavingRepository;
import com.spendwise.service.interfaces.ISavingService;
import com.spendwise.spec.SavingSpecification;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.spendwise.model.auth.User;

import java.math.BigDecimal;

@Service
public class SavingService implements ISavingService {

    private static final Logger log = LoggerFactory.getLogger(SavingService.class);
    private final ModelMapper modelMapper = new ModelMapper();

    private final SavingRepository savingRespository;

    @Autowired
    public SavingService(SavingRepository savingRespository) {
        this.savingRespository = savingRespository;
    }

    @Override
    public void populate(Saving saving, SavingDTO dto) {
        saving.setDescription(dto.getDescription());
        saving.setDate(dto.getDate());
        Currency currency = modelMapper.map(dto.getCurrency(), Currency.class);
        saving.setCurrency(currency);
        if (dto.getSavingsWallet() != null) {
            saving.setSavingsWallet(modelMapper.map(dto.getSavingsWallet(), SavingsWallet.class));
        }
        BigDecimal inputAmount = dto.getInputAmount();
        if (isPesosCurrency(currency)) {
            saving.setAmountInPesos(inputAmount);
            saving.setAmountInDollars(null);
        } else {
            saving.setAmountInDollars(inputAmount);
            saving.setAmountInPesos(null);
        }
    }

    private boolean isPesosCurrency(Currency currency) {
        if (currency == null || currency.getName() == null) return false;
        String name = currency.getName().toLowerCase();
        return name.contains("peso") || name.contains("ars") || name.contains("argentino");
    }

    @Transactional
    @Override
    public SavingDTO create(SavingDTO dto) {
        Saving category = new Saving();
        this.populate(category, dto);
        category.setUser(currentUser());
        Saving savedSaving = savingRespository.save(category);
        log.debug("Saving with id {} created successfully", savedSaving.getId());
        return modelMapper.map(savedSaving, SavingDTO.class);
    }

    @Transactional
    @Override
    public SavingDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        Saving category = find(id);
        log.debug("Saving with id {} read successfully", category.getId());
        return modelMapper.map(category, SavingDTO.class);
    }

    @Override
    public Page<SavingDTO> list(SavingFilterDTO filters, Pageable pageable) {
        log.debug("Listing all categories");
        Specification<Saving> spec = SavingSpecification.withFilters(filters, currentUser());
        return savingRespository.findAll(spec, pageable)
                .map(category -> modelMapper.map(category, SavingDTO.class));
    }

    @Transactional
    @Override
    public SavingDTO update(Long id, SavingDTO dto) throws ChangeSetPersister.NotFoundException {
        Saving category = find(id);
        this.populate(category, dto);
        Saving updatedSaving = savingRespository.save(category);
        log.debug("Saving with id {} updated successfully", category.getId());
        return modelMapper.map(updatedSaving, SavingDTO.class);
    }

    @Transactional
    @Override
    public SavingDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        Saving category = find(id);
        savingRespository.delete(category);
        log.debug("Saving with id {} deleted successfully", category.getId());
        return modelMapper.map(category, SavingDTO.class);
    }

    @Override
    public SavingDTO disable(Long id, SavingDTO dto) throws ChangeSetPersister.NotFoundException {
        return null;
    }


    protected Saving find(Long id) throws ChangeSetPersister.NotFoundException {
        return savingRespository.findByIdAndUser(id, currentUser())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
