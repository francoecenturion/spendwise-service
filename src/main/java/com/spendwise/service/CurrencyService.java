package com.spendwise.service;

import com.spendwise.dto.CurrencyDTO;
import com.spendwise.dto.CurrencyFilterDTO;
import com.spendwise.model.Currency;
import com.spendwise.repository.CurrencyRepository;
import com.spendwise.service.interfaces.ICurrencyService;
import com.spendwise.spec.CurrencyEspecification;
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

import com.spendwise.model.user.User;

@Service
public class CurrencyService implements ICurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyService.class);

    private final CurrencyRepository currencyRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public void populate(Currency currency, CurrencyDTO dto) {
        currency.setName(dto.getName());
        currency.setSymbol(dto.getSymbol());
    }

    @Transactional
    @Override
    public CurrencyDTO create(CurrencyDTO dto) {
        Currency currency = new Currency();
        this.populate(currency, dto);
        currency.setEnabled(true);
        currency.setUser(currentUser());
        Currency savedCurrency = currencyRepository.save(currency);
        log.debug("Currency with id {} created successfully", savedCurrency.getId());
        return modelMapper.map(savedCurrency, CurrencyDTO.class);
    }

    @Transactional
    @Override
    public CurrencyDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        Currency currency = find(id);
        log.debug("Currency with id {} read successfully", currency.getId());
        return modelMapper.map(currency, CurrencyDTO.class);
    }

    @Override
    public Page<CurrencyDTO> list(CurrencyFilterDTO filters, Pageable pageable) {
        log.debug("Listing all categories");

        Specification<Currency> spec = CurrencyEspecification.withFilters(filters, currentUser());

        return currencyRepository.findAll(spec, pageable)
                .map(currency -> modelMapper.map(currency, CurrencyDTO.class));
    }

    @Transactional
    @Override
    public CurrencyDTO update(Long id, CurrencyDTO dto) throws ChangeSetPersister.NotFoundException {
        Currency currency = find(id);
        this.populate(currency, dto);
        Currency updatedCurrency = currencyRepository.save(currency);
        log.debug("Currency with id {} updated successfully", currency.getId());
        return modelMapper.map(updatedCurrency, CurrencyDTO.class);
    }

    @Transactional
    @Override
    public CurrencyDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        Currency currency = find(id);
        currencyRepository.delete(currency);
        log.debug("Currency with id {} deleted successfully", currency.getId());
        return modelMapper.map(currency, CurrencyDTO.class);
    }

    @Transactional
    @Override
    public CurrencyDTO disable(Long id) throws ChangeSetPersister.NotFoundException {
        Currency currency = find(id);
        currency.setEnabled(false);
        Currency savedCurrency = currencyRepository.save(currency);
        log.debug("Currency with id {} disabled successfully", currency.getId());
        return modelMapper.map(savedCurrency, CurrencyDTO.class);
    }

    @Transactional
    @Override
    public CurrencyDTO enable(Long id) throws ChangeSetPersister.NotFoundException {
        Currency currency = find(id);
        currency.setEnabled(true);
        Currency savedCurrency = currencyRepository.save(currency);
        log.debug("Currency with id {} enabled successfully", currency.getId());
        return modelMapper.map(savedCurrency, CurrencyDTO.class);
    }

    protected Currency find(Long id) throws ChangeSetPersister.NotFoundException {
        return currencyRepository.findByIdAndUser(id, currentUser())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
