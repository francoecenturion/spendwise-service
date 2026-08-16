package com.spendwise.service;

import com.spendwise.dto.HistorySummaryDTO;
import com.spendwise.dto.MonthlySummaryDTO;
import com.spendwise.dto.YearlySummaryDTO;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.IncomeRepository;
import com.spendwise.service.interfaces.IHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.spendwise.model.auth.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HistoryService implements IHistoryService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    @Autowired
    public HistoryService(ExpenseRepository expenseRepository, IncomeRepository incomeRepository) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
    }

    @Override
    public HistorySummaryDTO getSummary() {
        User user = currentUser();

        List<Object[]> expenseRows = expenseRepository.getYearlySums(user);
        List<Object[]> incomeRows = incomeRepository.getYearlySums(user);
        List<Object[]> expenseMonthRows = expenseRepository.getMonthlySums(user);
        List<Object[]> incomeMonthRows = incomeRepository.getMonthlySums(user);

        Map<Integer, YearlySummaryDTO> byYear = new HashMap<>();

        for (Object[] row : expenseRows) {
            int year = ((Number) row[0]).intValue();
            BigDecimal ars = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            BigDecimal usd = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            YearlySummaryDTO dto = byYear.computeIfAbsent(year, y -> new YearlySummaryDTO(y, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new ArrayList<>()));
            dto.setExpensesARS(ars);
            dto.setExpensesUSD(usd);
        }

        for (Object[] row : incomeRows) {
            int year = ((Number) row[0]).intValue();
            BigDecimal ars = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            BigDecimal usd = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            YearlySummaryDTO dto = byYear.computeIfAbsent(year, y -> new YearlySummaryDTO(y, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new ArrayList<>()));
            dto.setIncomeARS(ars);
            dto.setIncomeUSD(usd);
        }

        // Build monthly map: year -> month -> dto
        Map<Integer, Map<Integer, MonthlySummaryDTO>> monthlyByYear = new HashMap<>();

        for (Object[] row : expenseMonthRows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal ars = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            BigDecimal usd = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;
            monthlyByYear.computeIfAbsent(year, y -> new HashMap<>())
                .computeIfAbsent(month, m -> new MonthlySummaryDTO(m, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
                .setExpensesARS(ars);
            monthlyByYear.get(year).get(month).setExpensesUSD(usd);
        }

        for (Object[] row : incomeMonthRows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal ars = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            BigDecimal usd = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;
            monthlyByYear.computeIfAbsent(year, y -> new HashMap<>())
                .computeIfAbsent(month, m -> new MonthlySummaryDTO(m, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
                .setIncomeARS(ars);
            monthlyByYear.get(year).get(month).setIncomeUSD(usd);
        }

        // Attach months to each year
        for (Map.Entry<Integer, YearlySummaryDTO> entry : byYear.entrySet()) {
            Map<Integer, MonthlySummaryDTO> monthMap = monthlyByYear.getOrDefault(entry.getKey(), new HashMap<>());
            List<MonthlySummaryDTO> months = new ArrayList<>(monthMap.values());
            months.sort((a, b) -> Integer.compare(a.getMonth(), b.getMonth()));
            entry.getValue().setMonths(months);
        }

        List<YearlySummaryDTO> years = new ArrayList<>(byYear.values());
        years.sort((a, b) -> Integer.compare(b.getYear(), a.getYear()));

        BigDecimal allTimeExpensesARS = years.stream().map(YearlySummaryDTO::getExpensesARS).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal allTimeExpensesUSD = years.stream().map(YearlySummaryDTO::getExpensesUSD).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal allTimeIncomeARS = years.stream().map(YearlySummaryDTO::getIncomeARS).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal allTimeIncomeUSD = years.stream().map(YearlySummaryDTO::getIncomeUSD).reduce(BigDecimal.ZERO, BigDecimal::add);

        HistorySummaryDTO result = new HistorySummaryDTO();
        result.setYears(years);
        result.setAllTimeExpensesARS(allTimeExpensesARS);
        result.setAllTimeExpensesUSD(allTimeExpensesUSD);
        result.setAllTimeIncomeARS(allTimeIncomeARS);
        result.setAllTimeIncomeUSD(allTimeIncomeUSD);
        return result;
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
