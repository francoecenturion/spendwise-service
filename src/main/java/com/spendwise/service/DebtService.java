package com.spendwise.service;

import com.spendwise.dto.DebtDTO;
import com.spendwise.dto.DebtFilterDTO;
import com.spendwise.model.Debt;
import com.spendwise.model.IssuingEntity;
import com.spendwise.model.PaymentMethod;
import com.spendwise.repository.DebtRepository;
import com.spendwise.service.interfaces.IDebtService;
import com.spendwise.spec.DebtSpecification;
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
public class DebtService implements IDebtService {

    private static final Logger log = LoggerFactory.getLogger(DebtService.class);

    private final DebtRepository debtRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public DebtService(DebtRepository debtRepository) {
        this.debtRepository = debtRepository;
    }

    @Override
    public void populate(Debt debt, DebtDTO dto) {
        debt.setDescription(dto.getDescription());
        debt.setAmountInPesos(dto.getAmountInPesos());
        debt.setAmountInDollars(dto.getAmountInDollars());
        debt.setDate(dto.getDate());
        debt.setDueDate(dto.getDueDate());
        debt.setPersonal(dto.getPersonal());
        debt.setCreditor(dto.getCreditor());
        debt.setIssuingEntity(dto.getIssuingEntity() != null
                ? modelMapper.map(dto.getIssuingEntity(), IssuingEntity.class)
                : null);
        debt.setPaymentMethod(dto.getPaymentMethod() != null
                ? modelMapper.map(dto.getPaymentMethod(), PaymentMethod.class)
                : null);
    }

    @Transactional
    @Override
    public DebtDTO create(DebtDTO dto) {
        Debt debt = new Debt();
        this.populate(debt, dto);
        debt.setCancelled(false);
        Debt saved = debtRepository.save(debt);
        log.debug("Debt with id {} created successfully", saved.getId());
        return modelMapper.map(saved, DebtDTO.class);
    }

    @Transactional
    @Override
    public DebtDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        Debt debt = find(id);
        log.debug("Debt with id {} read successfully", debt.getId());
        return modelMapper.map(debt, DebtDTO.class);
    }

    @Override
    public Page<DebtDTO> list(DebtFilterDTO filters, Pageable pageable) {
        log.debug("Listing all debts");
        Specification<Debt> spec = DebtSpecification.withFilters(filters);
        return debtRepository.findAll(spec, pageable)
                .map(debt -> modelMapper.map(debt, DebtDTO.class));
    }

    @Transactional
    @Override
    public DebtDTO update(Long id, DebtDTO dto) throws ChangeSetPersister.NotFoundException {
        Debt debt = find(id);
        this.populate(debt, dto);
        Debt updated = debtRepository.save(debt);
        log.debug("Debt with id {} updated successfully", debt.getId());
        return modelMapper.map(updated, DebtDTO.class);
    }

    @Transactional
    @Override
    public DebtDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        Debt debt = find(id);
        debtRepository.delete(debt);
        log.debug("Debt with id {} deleted successfully", debt.getId());
        return modelMapper.map(debt, DebtDTO.class);
    }

    @Transactional
    @Override
    public DebtDTO cancel(Long id) throws ChangeSetPersister.NotFoundException {
        Debt debt = find(id);
        debt.setCancelled(true);
        Debt saved = debtRepository.save(debt);
        log.debug("Debt with id {} cancelled successfully", debt.getId());
        return modelMapper.map(saved, DebtDTO.class);
    }

    @Transactional
    @Override
    public DebtDTO uncancel(Long id) throws ChangeSetPersister.NotFoundException {
        Debt debt = find(id);
        debt.setCancelled(false);
        Debt saved = debtRepository.save(debt);
        log.debug("Debt with id {} uncancelled successfully", debt.getId());
        return modelMapper.map(saved, DebtDTO.class);
    }

    protected Debt find(Long id) throws ChangeSetPersister.NotFoundException {
        return debtRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

}
