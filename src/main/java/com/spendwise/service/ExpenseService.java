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
import com.spendwise.model.RecurrentExpense;
import com.spendwise.model.RecurrentExpenseRecord;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.RecurrentExpenseRecordRepository;
import com.spendwise.repository.RecurrentExpenseRepository;
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

import com.spendwise.model.auth.User;

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
    private final RecurrentExpenseRepository recurrentExpenseRepository;
    private final RecurrentExpenseRecordRepository recurrentExpenseRecordRepository;

    @Autowired
    public ExpenseService(
        ExpenseRepository expenseRespository,
        DolarApiClient dolarApiClient,
        DolarApiHistoricalClient dolarApiHistoricalClient,
        RecurrentExpenseRepository recurrentExpenseRepository,
        RecurrentExpenseRecordRepository recurrentExpenseRecordRepository
    ) {
        this.expenseRespository = expenseRespository;
        this.dolarApiClient = dolarApiClient;
        this.dolarApiHistoricalClient = dolarApiHistoricalClient;
        this.recurrentExpenseRepository = recurrentExpenseRepository;
        this.recurrentExpenseRecordRepository = recurrentExpenseRecordRepository;
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
        Expense expense = new Expense();
        this.populate(expense, dto);
        User user = currentUser();
        expense.setUser(user);
        Expense savedExpense = expenseRespository.save(expense);

        autoCancelRecurrentExpense(savedExpense, user);

        log.debug("Expense with id {} created successfully", savedExpense.getId());
        return modelMapper.map(savedExpense, ExpenseDTO.class);
    }

    private void autoCancelRecurrentExpense(Expense savedExpense, User user) {
        recurrentExpenseRepository
                .findByDescriptionIgnoreCaseAndUserAndEnabledTrue(savedExpense.getDescription(), user)
                .ifPresent(recurrentExpense -> {
                    LocalDate today = LocalDate.now();
                    int month = today.getMonthValue();
                    int year = today.getYear();

                    RecurrentExpenseRecord record = recurrentExpenseRecordRepository
                            .findByRecurrentExpenseAndMonthAndYear(recurrentExpense, month, year)
                            .orElseGet(() -> {
                                RecurrentExpenseRecord newRecord = new RecurrentExpenseRecord();
                                newRecord.setRecurrentExpense(recurrentExpense);
                                newRecord.setMonth(month);
                                newRecord.setYear(year);
                                newRecord.setUser(user);
                                return newRecord;
                            });

                    record.setCancelled(true);
                    record.setExpense(savedExpense);
                    recurrentExpenseRecordRepository.save(record);
                    log.debug("RecurrentExpenseRecord auto-cancelled for recurrentExpense id {} ({}/{})",
                            recurrentExpense.getId(), month, year);

                    syncAmountIfChanged(savedExpense, recurrentExpense);
                });
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


    private void syncAmountIfChanged(Expense expense, RecurrentExpense recurrentExpense) {
        boolean isPesos = isPesosCurrency(expense.getCurrency());

        BigDecimal expenseAmount  = isPesos ? expense.getAmountInPesos() : expense.getAmountInDollars();
        BigDecimal recurrentAmount = isPesos ? recurrentExpense.getAmountInPesos() : recurrentExpense.getAmountInDollars();

        if (expenseAmount == null) return;

        if (recurrentAmount == null || expenseAmount.compareTo(recurrentAmount) != 0) {
            recurrentExpense.setAmountInPesos(expense.getAmountInPesos());
            recurrentExpense.setAmountInDollars(expense.getAmountInDollars());
            recurrentExpenseRepository.save(recurrentExpense);
            log.debug("RecurrentExpense id {} amount updated to ARS={} USD={}",
                    recurrentExpense.getId(), expense.getAmountInPesos(), expense.getAmountInDollars());
        }
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
