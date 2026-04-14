package com.spendwise.service;

import com.spendwise.dto.PersonalDebtDTO;
import com.spendwise.dto.PersonalDebtFilterDTO;
import com.spendwise.model.Currency;
import com.spendwise.model.PersonalDebt;
import com.spendwise.repository.PersonalDebtRepository;
import com.spendwise.service.interfaces.IPersonalDebtService;
import com.spendwise.spec.PersonalDebtSpecification;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.spendwise.model.auth.User;

import java.math.BigDecimal;

@Service
public class PersonalDebtService implements IPersonalDebtService {

    private static final Logger log = LoggerFactory.getLogger(PersonalDebtService.class);

    private final PersonalDebtRepository personalDebtRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    public PersonalDebtService(PersonalDebtRepository personalDebtRepository) {
        this.personalDebtRepository = personalDebtRepository;
    }

    @Override
    public void populate(PersonalDebt entity, PersonalDebtDTO dto) {
        entity.setDescription(dto.getDescription());
        entity.setDate(dto.getDate());
        entity.setDueDate(dto.getDueDate());
        entity.setCreditor(dto.getCreditor());

        if (dto.getCurrency() != null && dto.getCurrency().getId() != null) {
            Currency currency = modelMapper.map(dto.getCurrency(), Currency.class);
            entity.setCurrency(currency);
            BigDecimal inputAmount = dto.getInputAmount() != null ? dto.getInputAmount() : dto.getAmountInPesos();
            if (isPesosCurrency(currency)) {
                entity.setAmountInPesos(inputAmount);
                entity.setAmountInDollars(null);
            } else {
                entity.setAmountInDollars(inputAmount);
                entity.setAmountInPesos(null);
            }
        } else {
            entity.setAmountInPesos(dto.getAmountInPesos());
            entity.setAmountInDollars(dto.getAmountInDollars());
        }
    }

    private boolean isPesosCurrency(Currency currency) {
        if (currency == null || currency.getName() == null) return true;
        String name = currency.getName().toLowerCase();
        return name.contains("peso") || name.contains("ars") || name.contains("argentino");
    }

    @Transactional
    @Override
    public PersonalDebtDTO create(PersonalDebtDTO dto) {
        PersonalDebt entity = new PersonalDebt();
        this.populate(entity, dto);
        entity.setCancelled(false);
        entity.setUser(currentUser());
        PersonalDebt saved = personalDebtRepository.save(entity);
        log.debug("PersonalDebt with id {} created successfully", saved.getId());
        return modelMapper.map(saved, PersonalDebtDTO.class);
    }

    @Override
    public PersonalDebtDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        PersonalDebt entity = find(id);
        return modelMapper.map(entity, PersonalDebtDTO.class);
    }

    @Override
    public Page<PersonalDebtDTO> list(PersonalDebtFilterDTO filters, Pageable pageable) {
        Specification<PersonalDebt> spec = PersonalDebtSpecification.withFilters(filters, currentUser());
        return personalDebtRepository.findAll(spec, pageable)
                .map(e -> modelMapper.map(e, PersonalDebtDTO.class));
    }

    @Transactional
    @Override
    public PersonalDebtDTO update(Long id, PersonalDebtDTO dto) throws ChangeSetPersister.NotFoundException {
        PersonalDebt entity = find(id);
        this.populate(entity, dto);
        PersonalDebt updated = personalDebtRepository.save(entity);
        log.debug("PersonalDebt with id {} updated successfully", id);
        return modelMapper.map(updated, PersonalDebtDTO.class);
    }

    @Transactional
    @Override
    public PersonalDebtDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        PersonalDebt entity = find(id);
        personalDebtRepository.delete(entity);
        log.debug("PersonalDebt with id {} deleted successfully", id);
        return modelMapper.map(entity, PersonalDebtDTO.class);
    }

    @Transactional
    @Override
    public PersonalDebtDTO cancel(Long id) throws ChangeSetPersister.NotFoundException {
        PersonalDebt entity = find(id);
        entity.setCancelled(true);
        PersonalDebt saved = personalDebtRepository.save(entity);
        log.debug("PersonalDebt with id {} cancelled successfully", id);
        return modelMapper.map(saved, PersonalDebtDTO.class);
    }

    @Transactional
    @Override
    public PersonalDebtDTO uncancel(Long id) throws ChangeSetPersister.NotFoundException {
        PersonalDebt entity = find(id);
        entity.setCancelled(false);
        PersonalDebt saved = personalDebtRepository.save(entity);
        log.debug("PersonalDebt with id {} uncancelled successfully", id);
        return modelMapper.map(saved, PersonalDebtDTO.class);
    }

    protected PersonalDebt find(Long id) throws ChangeSetPersister.NotFoundException {
        return personalDebtRepository.findByIdAndUser(id, currentUser())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
