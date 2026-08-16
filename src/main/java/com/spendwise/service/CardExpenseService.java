package com.spendwise.service;

import com.spendwise.dto.CardExpenseDTO;
import com.spendwise.dto.CardExpenseFilterDTO;
import com.spendwise.model.CardExpense;
import com.spendwise.model.Currency;
import com.spendwise.model.PaymentMethod;
import com.spendwise.repository.CardExpenseRepository;
import com.spendwise.service.interfaces.ICardExpenseService;
import com.spendwise.spec.CardExpenseSpecification;
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
public class CardExpenseService implements ICardExpenseService {

    private static final Logger log = LoggerFactory.getLogger(CardExpenseService.class);

    private final CardExpenseRepository cardExpenseRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    public CardExpenseService(CardExpenseRepository cardExpenseRepository) {
        this.cardExpenseRepository = cardExpenseRepository;
    }

    @Override
    public void populate(CardExpense entity, CardExpenseDTO dto) {
        entity.setDescription(dto.getDescription());
        entity.setDate(dto.getDate());
        entity.setDueDate(dto.getDueDate());
        entity.setPaymentMethod(dto.getPaymentMethod() != null
                ? modelMapper.map(dto.getPaymentMethod(), PaymentMethod.class)
                : null);

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
    public CardExpenseDTO create(CardExpenseDTO dto) {
        CardExpense entity = new CardExpense();
        this.populate(entity, dto);
        entity.setCancelled(false);
        entity.setUser(currentUser());
        CardExpense saved = cardExpenseRepository.save(entity);
        log.debug("CardExpense with id {} created successfully", saved.getId());
        return modelMapper.map(saved, CardExpenseDTO.class);
    }

    @Override
    public CardExpenseDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        CardExpense entity = find(id);
        return modelMapper.map(entity, CardExpenseDTO.class);
    }

    @Override
    public Page<CardExpenseDTO> list(CardExpenseFilterDTO filters, Pageable pageable) {
        Specification<CardExpense> spec = CardExpenseSpecification.withFilters(filters, currentUser());
        return cardExpenseRepository.findAll(spec, pageable)
                .map(e -> modelMapper.map(e, CardExpenseDTO.class));
    }

    @Transactional
    @Override
    public CardExpenseDTO update(Long id, CardExpenseDTO dto) throws ChangeSetPersister.NotFoundException {
        CardExpense entity = find(id);
        this.populate(entity, dto);
        CardExpense updated = cardExpenseRepository.save(entity);
        log.debug("CardExpense with id {} updated successfully", id);
        return modelMapper.map(updated, CardExpenseDTO.class);
    }

    @Transactional
    @Override
    public CardExpenseDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        CardExpense entity = find(id);
        cardExpenseRepository.delete(entity);
        log.debug("CardExpense with id {} deleted successfully", id);
        return modelMapper.map(entity, CardExpenseDTO.class);
    }

    @Transactional
    @Override
    public CardExpenseDTO cancel(Long id) throws ChangeSetPersister.NotFoundException {
        CardExpense entity = find(id);
        entity.setCancelled(true);
        CardExpense saved = cardExpenseRepository.save(entity);
        log.debug("CardExpense with id {} cancelled successfully", id);
        return modelMapper.map(saved, CardExpenseDTO.class);
    }

    @Transactional
    @Override
    public CardExpenseDTO uncancel(Long id) throws ChangeSetPersister.NotFoundException {
        CardExpense entity = find(id);
        entity.setCancelled(false);
        CardExpense saved = cardExpenseRepository.save(entity);
        log.debug("CardExpense with id {} uncancelled successfully", id);
        return modelMapper.map(saved, CardExpenseDTO.class);
    }

    protected CardExpense find(Long id) throws ChangeSetPersister.NotFoundException {
        return cardExpenseRepository.findByIdAndUser(id, currentUser())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
