package com.spendwise.spec;

import com.spendwise.dto.SavingFilterDTO;
import com.spendwise.model.Saving;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SavingSpecification {

    public static Specification<Saving> withFilters(SavingFilterDTO filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por descripción (búsqueda parcial, case-insensitive)
            if (filters.getDescription() != null && !filters.getDescription().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")),
                        "%" + filters.getDescription().toLowerCase() + "%"));
            }

            // Filtros por monto en pesos
            if (filters.getMinAmountInPesos() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amountInPesos"),
                        filters.getMinAmountInPesos()));
            }

            if (filters.getMaxAmountInPesos() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amountInPesos"),
                        filters.getMaxAmountInPesos()));
            }

            // Filtros por monto en dólares
            if (filters.getMinAmountInDollars() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amountInDollars"),
                        filters.getMinAmountInDollars()));
            }

            if (filters.getMaxAmountInDollars() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amountInDollars"),
                        filters.getMaxAmountInDollars()));
            }

            // Filtros por rango de fechas
            if (filters.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"),
                        filters.getStartDate()));
            }

            if (filters.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"),
                        filters.getEndDate()));
            }

            if (filters.getCurrencyId() != null) {
                predicates.add(cb.equal(root.get("currency").get("id"),
                        filters.getCurrencyId()));
            }

            if (filters.getSavingsWalletId() != null) {
                predicates.add(cb.equal(root.get("savingsWallet").get("id"),
                        filters.getSavingsWalletId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}