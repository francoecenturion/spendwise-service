package com.spendwise.service;

import com.spendwise.dto.BudgetDTO;
import com.spendwise.dto.BudgetFilterDTO;
import com.spendwise.dto.RecurrentExpenseDTO;
import com.spendwise.model.Budget;
import com.spendwise.model.RecurrentExpense;
import com.spendwise.model.RecurrentExpenseRecord;
import com.spendwise.model.auth.User;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.RecurrentExpenseRecordRepository;
import com.spendwise.repository.RecurrentExpenseRepository;
import com.spendwise.service.interfaces.IBudgetService;
import com.spendwise.spec.BudgetSpecification;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService implements IBudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final BudgetRepository budgetRepository;
    private final RecurrentExpenseRepository recurrentExpenseRepository;
    private final RecurrentExpenseRecordRepository recurrentExpenseRecordRepository;

    @Autowired
    public BudgetService(BudgetRepository budgetRepository,
                         RecurrentExpenseRepository recurrentExpenseRepository,
                         RecurrentExpenseRecordRepository recurrentExpenseRecordRepository) {
        this.budgetRepository = budgetRepository;
        this.recurrentExpenseRepository = recurrentExpenseRepository;
        this.recurrentExpenseRecordRepository = recurrentExpenseRecordRepository;
    }

    @Override
    public void populate(Budget budget, BudgetDTO dto) {
        budget.setDescription(dto.getDescription());
        budget.setMonth(dto.getMonth());
        budget.setYear(dto.getYear());
        if (dto.getRecurrentExpenses() != null) {
            List<RecurrentExpense> expenses = new ArrayList<>();
            for (RecurrentExpenseDTO reDTO : dto.getRecurrentExpenses()) {
                if (reDTO.getId() != null) {
                    recurrentExpenseRepository.findById(reDTO.getId()).ifPresent(expenses::add);
                }
            }
            budget.setRecurrentExpenses(expenses);
        }
    }

    @Transactional
    @Override
    public BudgetDTO create(BudgetDTO dto) {
        Budget budget = new Budget();
        this.populate(budget, dto);
        budget.setEnabled(true);
        budget.setUser(currentUser());
        Budget saved = budgetRepository.save(budget);
        log.debug("Budget with id {} created successfully", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    @Override
    public BudgetDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        Budget budget = find(id);
        log.debug("Budget with id {} read successfully", budget.getId());
        return toDTO(budget);
    }

    @Override
    public Page<BudgetDTO> list(BudgetFilterDTO filters, Pageable pageable) {
        log.debug("Listing all budgets");
        Specification<Budget> spec = BudgetSpecification.withFilters(filters, currentUser());
        return budgetRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Transactional
    @Override
    public BudgetDTO update(Long id, BudgetDTO dto) throws ChangeSetPersister.NotFoundException {
        Budget budget = find(id);
        this.populate(budget, dto);
        Budget updated = budgetRepository.save(budget);
        log.debug("Budget with id {} updated successfully", updated.getId());
        return toDTO(updated);
    }

    @Transactional
    @Override
    public BudgetDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        Budget budget = find(id);
        budgetRepository.delete(budget);
        log.debug("Budget with id {} deleted successfully", budget.getId());
        return toDTO(budget);
    }

    @Transactional
    @Override
    public BudgetDTO enable(Long id) throws ChangeSetPersister.NotFoundException {
        Budget budget = find(id);
        budget.setEnabled(true);
        budgetRepository.save(budget);
        log.debug("Budget with id {} enabled successfully", budget.getId());
        return toDTO(budget);
    }

    @Transactional
    @Override
    public BudgetDTO disable(Long id) throws ChangeSetPersister.NotFoundException {
        Budget budget = find(id);
        budget.setEnabled(false);
        budgetRepository.save(budget);
        log.debug("Budget with id {} disabled successfully", budget.getId());
        return toDTO(budget);
    }

    private BudgetDTO toDTO(Budget budget) {
        BudgetDTO dto = new BudgetDTO();
        dto.setId(budget.getId());
        dto.setDescription(budget.getDescription());
        dto.setMonth(budget.getMonth());
        dto.setYear(budget.getYear());
        dto.setEnabled(budget.getEnabled());

        List<RecurrentExpenseDTO> reDTOs = new ArrayList<>();
        BigDecimal totalExpectedARS = BigDecimal.ZERO;
        BigDecimal totalExpectedUSD = BigDecimal.ZERO;
        BigDecimal totalCancelledARS = BigDecimal.ZERO;
        BigDecimal totalCancelledUSD = BigDecimal.ZERO;
        int cancelledCount = 0;

        for (RecurrentExpense re : budget.getRecurrentExpenses()) {
            reDTOs.add(modelMapper.map(re, RecurrentExpenseDTO.class));

            if (re.getAmountInPesos() != null) {
                totalExpectedARS = totalExpectedARS.add(re.getAmountInPesos());
            }
            if (re.getAmountInDollars() != null) {
                totalExpectedUSD = totalExpectedUSD.add(re.getAmountInDollars());
            }

            Optional<RecurrentExpenseRecord> record = recurrentExpenseRecordRepository
                    .findByRecurrentExpenseAndMonthAndYear(re, budget.getMonth(), budget.getYear());
            if (record.isPresent() && Boolean.TRUE.equals(record.get().getCancelled())) {
                cancelledCount++;
                if (re.getAmountInPesos() != null) {
                    totalCancelledARS = totalCancelledARS.add(re.getAmountInPesos());
                }
                if (re.getAmountInDollars() != null) {
                    totalCancelledUSD = totalCancelledUSD.add(re.getAmountInDollars());
                }
            }
        }

        dto.setRecurrentExpenses(reDTOs);
        dto.setTotalExpectedARS(totalExpectedARS);
        dto.setTotalExpectedUSD(totalExpectedUSD);
        dto.setTotalCancelledARS(totalCancelledARS);
        dto.setTotalCancelledUSD(totalCancelledUSD);
        dto.setCancelledCount(cancelledCount);
        dto.setPendingCount(reDTOs.size() - cancelledCount);

        return dto;
    }

    protected Budget find(Long id) throws ChangeSetPersister.NotFoundException {
        return budgetRepository.findByIdAndUser(id, currentUser())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
