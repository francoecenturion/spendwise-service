package com.spendwise.service;

import com.spendwise.client.dolarApi.DolarApiClient;
import com.spendwise.client.dolarApi.DolarApiDTO;
import com.spendwise.client.dolarApiHistorical.DolarApiHistoricalClient;
import com.spendwise.client.dolarApiHistorical.DolarApiHistoricalDTO;
import com.spendwise.dto.IncomeDTO;
import com.spendwise.dto.IncomeFilterDTO;
import com.spendwise.model.Category;
import com.spendwise.model.Income;
import com.spendwise.model.PaymentMethod;
import com.spendwise.repository.IncomeRepository;
import com.spendwise.service.interfaces.IIncomeService;
import com.spendwise.spec.IncomeSpecification;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class IncomeService implements IIncomeService {

    private static final Logger log = LoggerFactory.getLogger(IncomeService.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final DolarApiClient dolarApiClient;
    private final DolarApiHistoricalClient dolarApiHistoricalClient;

    private final IncomeRepository incomeRepository;

    @Autowired
    public IncomeService(
        IncomeRepository incomeRepository,
        DolarApiClient dolarApiClient,
        DolarApiHistoricalClient dolarApiHistoricalClient
    ) {
        this.incomeRepository = incomeRepository;
        this.dolarApiClient = dolarApiClient;
        this.dolarApiHistoricalClient = dolarApiHistoricalClient;
    }

    @Override
    public void populate(Income income, IncomeDTO dto) {
        income.setDescription(dto.getDescription());
        income.setAmountInPesos(dto.getAmountInPesos());
        income.setAmountInDollars(this.calculateAmountInDollars(dto.getAmountInPesos(), dto.getDate()));
        income.setSource(modelMapper.map(dto.getSource(), Category.class));
        income.setDate(dto.getDate());
    }

    @Transactional
    @Override
    public IncomeDTO create(IncomeDTO dto) {
        Income income = new Income();
        this.populate(income, dto);
        income.setUser(currentUser());
        Income savedIncome = incomeRepository.save(income);
        log.debug("Income with id {} created successfully", savedIncome.getId());
        return modelMapper.map(savedIncome, IncomeDTO.class);
    }

    @Transactional
    @Override
    public IncomeDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        Income income = find(id);
        log.debug("Income with id {} read successfully", income.getId());
        return modelMapper.map(income, IncomeDTO.class);
    }

    @Override
    public Page<IncomeDTO> list(IncomeFilterDTO filters, Pageable pageable) {
        log.debug("Listing all categories");
        Specification<Income> spec = IncomeSpecification.withFilters(filters, currentUser());
        return incomeRepository.findAll(spec, pageable)
                .map(income -> modelMapper.map(income, IncomeDTO.class));
    }

    @Transactional
    @Override
    public IncomeDTO update(Long id, IncomeDTO dto) throws ChangeSetPersister.NotFoundException {
        Income income = find(id);
        this.populate(income, dto);
        Income updatedIncome = incomeRepository.save(income);
        log.debug("Income with id {} updated successfully", income.getId());
        return modelMapper.map(updatedIncome, IncomeDTO.class);
    }

    @Transactional
    @Override
    public IncomeDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        Income income = find(id);
        incomeRepository.delete(income);
        log.debug("Income with id {} deleted successfully", income.getId());
        return modelMapper.map(income, IncomeDTO.class);
    }

    @Override
    public IncomeDTO disable(Long id, IncomeDTO dto) throws ChangeSetPersister.NotFoundException {
        return null;
    }


    protected Income find(Long id) throws ChangeSetPersister.NotFoundException {
        return incomeRepository.findByIdAndUser(id, currentUser())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public BigDecimal calculateAmountInDollars(BigDecimal amountInPesos, LocalDate date) {

        BigDecimal amountInDollars;

        if(LocalDate.now().isEqual(date)) {
            // Dolar Api
            DolarApiDTO dolarApiDTO = dolarApiClient.getRate("oficial");
            amountInDollars = amountInPesos.divide(dolarApiDTO.getSellingPrice(), 4, RoundingMode.HALF_EVEN);
        } else {
            // Dolar Api Historical
            DolarApiHistoricalDTO dolarApiHistoricalDTO = dolarApiHistoricalClient.getRate("oficial", date.toString());
            amountInDollars = amountInPesos.divide(dolarApiHistoricalDTO.getSellingPrice(), 4,  RoundingMode.HALF_EVEN);
        }

        return amountInDollars;
    }
}
