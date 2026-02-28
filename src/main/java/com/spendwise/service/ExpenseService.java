package com.spendwise.service;

import com.spendwise.client.dolarApi.DolarApiClient;
import com.spendwise.client.dolarApiHistorical.DolarApiHistoricalClient;
import com.spendwise.client.dolarApi.DolarApiDTO;
import com.spendwise.client.dolarApiHistorical.DolarApiHistoricalDTO;
import com.spendwise.dto.ExpenseDTO;
import com.spendwise.dto.ExpenseFilterDTO;
import com.spendwise.model.Category;
import com.spendwise.model.Currency;
import com.spendwise.model.Expense;
import com.spendwise.model.PaymentMethod;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.service.interfaces.IExpenseService;
import com.spendwise.spec.ExpenseSpecification;
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
public class ExpenseService implements IExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final DolarApiClient dolarApiClient;
    private final DolarApiHistoricalClient dolarApiHistoricalClient;

    private final ExpenseRepository expenseRespository;

    @Autowired
    public ExpenseService(
        ExpenseRepository expenseRespository,
        DolarApiClient dolarApiClient,
        DolarApiHistoricalClient dolarApiHistoricalClient
    ) {
        this.expenseRespository = expenseRespository;
        this.dolarApiClient = dolarApiClient;
        this.dolarApiHistoricalClient = dolarApiHistoricalClient;
    }

    @Override
    public void populate(Expense expense, ExpenseDTO dto) {
        expense.setDescription(dto.getDescription());
        expense.setDate(dto.getDate());
        expense.setCategory(modelMapper.map(dto.getCategory(), Category.class));
        expense.setPaymentMethod(modelMapper.map(dto.getPaymentMethod(), PaymentMethod.class));
        expense.setIsMicroExpense(dto.getMicroExpense());

        if (dto.getCurrency() != null && dto.getCurrency().getId() != null) {
            Currency currency = modelMapper.map(dto.getCurrency(), Currency.class);
            expense.setCurrency(currency);
            BigDecimal inputAmount = dto.getInputAmount() != null ? dto.getInputAmount() : dto.getAmountInPesos();
            if (isPesosCurrency(currency)) {
                expense.setAmountInPesos(inputAmount);
                expense.setAmountInDollars(this.calculateAmountInDollars(inputAmount, dto.getDate()));
            } else {
                expense.setAmountInDollars(inputAmount);
                expense.setAmountInPesos(this.calculateAmountInPesos(inputAmount, dto.getDate()));
            }
        } else {
            expense.setAmountInPesos(dto.getAmountInPesos());
            expense.setAmountInDollars(this.calculateAmountInDollars(dto.getAmountInPesos(), dto.getDate()));
        }
    }

    private boolean isPesosCurrency(Currency currency) {
        if (currency == null || currency.getName() == null) return true;
        String name = currency.getName().toLowerCase();
        return name.contains("peso") || name.contains("ars") || name.contains("argentino");
    }

    @Transactional
    @Override
    public ExpenseDTO create(ExpenseDTO dto) {
        Expense category = new Expense();
        this.populate(category, dto);
        category.setUser(currentUser());
        Expense savedExpense = expenseRespository.save(category);
        log.debug("Expense with id {} created successfully", savedExpense.getId());
        return modelMapper.map(savedExpense, ExpenseDTO.class);
    }

    @Transactional
    @Override
    public ExpenseDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        Expense category = find(id);
        log.debug("Expense with id {} read successfully", category.getId());
        return modelMapper.map(category, ExpenseDTO.class);
    }

    @Override
    public Page<ExpenseDTO> list(ExpenseFilterDTO filters, Pageable pageable) {
        log.debug("Listing all categories");
        Specification<Expense> spec = ExpenseSpecification.withFilters(filters, currentUser());
        return expenseRespository.findAll(spec, pageable)
                .map(category -> modelMapper.map(category, ExpenseDTO.class));
    }

    @Transactional
    @Override
    public ExpenseDTO update(Long id, ExpenseDTO dto) throws ChangeSetPersister.NotFoundException {
        Expense category = find(id);
        this.populate(category, dto);
        Expense updatedExpense = expenseRespository.save(category);
        log.debug("Expense with id {} updated successfully", category.getId());
        return modelMapper.map(updatedExpense, ExpenseDTO.class);
    }

    @Transactional
    @Override
    public ExpenseDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        Expense category = find(id);
        expenseRespository.delete(category);
        log.debug("Expense with id {} deleted successfully", category.getId());
        return modelMapper.map(category, ExpenseDTO.class);
    }

    @Override
    public ExpenseDTO disable(Long id, ExpenseDTO dto) throws ChangeSetPersister.NotFoundException {
        return null;
    }


    protected Expense find(Long id) throws ChangeSetPersister.NotFoundException {
        return expenseRespository.findByIdAndUser(id, currentUser())
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

    public BigDecimal calculateAmountInPesos(BigDecimal amountInDollars, LocalDate date) {

        BigDecimal amountInPesos;

        if(LocalDate.now().isEqual(date)) {
            // Dolar Api
            DolarApiDTO dolarApiDTO = dolarApiClient.getRate("oficial");
            amountInPesos = amountInDollars.multiply(dolarApiDTO.getSellingPrice());
        } else {
            // Dolar Api Historical
            DolarApiHistoricalDTO dolarApiHistoricalDTO = dolarApiHistoricalClient.getRate("oficial", date.toString());
            amountInPesos = amountInDollars.multiply(dolarApiHistoricalDTO.getSellingPrice());
        }

        return amountInPesos;
    }
}
