package com.spendwise.service;

import com.spendwise.dto.RecurrentExpenseDTO;
import com.spendwise.dto.RecurrentExpenseFilterDTO;
import com.spendwise.model.Category;
import com.spendwise.model.Currency;
import com.spendwise.model.PaymentMethod;
import com.spendwise.model.RecurrentExpense;
import com.spendwise.model.auth.User;
import com.spendwise.repository.RecurrentExpenseRepository;
import com.spendwise.service.interfaces.IRecurrentExpenseService;
import com.spendwise.spec.RecurrentExpenseSpecification;
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

@Service
public class RecurrentExpenseService implements IRecurrentExpenseService {

    private static final Logger log = LoggerFactory.getLogger(RecurrentExpenseService.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final RecurrentExpenseRepository recurrentExpenseRepository;

    @Autowired
    public RecurrentExpenseService(RecurrentExpenseRepository recurrentExpenseRepository) {
        this.recurrentExpenseRepository = recurrentExpenseRepository;
    }

    @Override
    public void populate(RecurrentExpense recurrentExpense, RecurrentExpenseDTO dto) {
        recurrentExpense.setDescription(dto.getDescription());
        recurrentExpense.setIcon(dto.getIcon());
        recurrentExpense.setAmountInPesos(dto.getAmountInPesos());
        recurrentExpense.setAmountInDollars(dto.getAmountInDollars());
        recurrentExpense.setDayOfMonth(dto.getDayOfMonth());
        if (dto.getCategory() != null) {
            recurrentExpense.setCategory(modelMapper.map(dto.getCategory(), Category.class));
        }
        if (dto.getPaymentMethod() != null) {
            recurrentExpense.setPaymentMethod(modelMapper.map(dto.getPaymentMethod(), PaymentMethod.class));
        }
        if (dto.getCurrency() != null) {
            recurrentExpense.setCurrency(modelMapper.map(dto.getCurrency(), Currency.class));
        }
    }

    @Transactional
    @Override
    public RecurrentExpenseDTO create(RecurrentExpenseDTO dto) {
        RecurrentExpense recurrentExpense = new RecurrentExpense();
        this.populate(recurrentExpense, dto);
        recurrentExpense.setEnabled(true);
        recurrentExpense.setUser(currentUser());
        RecurrentExpense saved = recurrentExpenseRepository.save(recurrentExpense);
        log.debug("RecurrentExpense with id {} created successfully", saved.getId());
        return modelMapper.map(saved, RecurrentExpenseDTO.class);
    }

    @Transactional
    @Override
    public RecurrentExpenseDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpense recurrentExpense = find(id);
        log.debug("RecurrentExpense with id {} read successfully", recurrentExpense.getId());
        return modelMapper.map(recurrentExpense, RecurrentExpenseDTO.class);
    }

    @Override
    public Page<RecurrentExpenseDTO> list(RecurrentExpenseFilterDTO filters, Pageable pageable) {
        log.debug("Listing all recurrent expenses");
        Specification<RecurrentExpense> spec = RecurrentExpenseSpecification.withFilters(filters, currentUser());
        return recurrentExpenseRepository.findAll(spec, pageable)
                .map(re -> modelMapper.map(re, RecurrentExpenseDTO.class));
    }

    @Transactional
    @Override
    public RecurrentExpenseDTO update(Long id, RecurrentExpenseDTO dto) throws ChangeSetPersister.NotFoundException {
        RecurrentExpense recurrentExpense = find(id);
        this.populate(recurrentExpense, dto);
        RecurrentExpense updated = recurrentExpenseRepository.save(recurrentExpense);
        log.debug("RecurrentExpense with id {} updated successfully", updated.getId());
        return modelMapper.map(updated, RecurrentExpenseDTO.class);
    }

    @Transactional
    @Override
    public RecurrentExpenseDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpense recurrentExpense = find(id);
        recurrentExpenseRepository.delete(recurrentExpense);
        log.debug("RecurrentExpense with id {} deleted successfully", recurrentExpense.getId());
        return modelMapper.map(recurrentExpense, RecurrentExpenseDTO.class);
    }

    @Transactional
    @Override
    public RecurrentExpenseDTO enable(Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpense recurrentExpense = find(id);
        recurrentExpense.setEnabled(true);
        recurrentExpenseRepository.save(recurrentExpense);
        log.debug("RecurrentExpense with id {} enabled successfully", recurrentExpense.getId());
        return modelMapper.map(recurrentExpense, RecurrentExpenseDTO.class);
    }

    @Transactional
    @Override
    public RecurrentExpenseDTO disable(Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpense recurrentExpense = find(id);
        recurrentExpense.setEnabled(false);
        recurrentExpenseRepository.save(recurrentExpense);
        log.debug("RecurrentExpense with id {} disabled successfully", recurrentExpense.getId());
        return modelMapper.map(recurrentExpense, RecurrentExpenseDTO.class);
    }

    protected RecurrentExpense find(Long id) throws ChangeSetPersister.NotFoundException {
        return recurrentExpenseRepository.findByIdAndUser(id, currentUser())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
