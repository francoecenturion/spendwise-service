package com.spendwise.spec;

import com.spendwise.dto.IncomeFilterDTO;
import com.spendwise.model.Income;
import com.spendwise.model.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class IncomeSpecification {

    public static Specification<Income> withFilters(IncomeFilterDTO filters, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            if (filters.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filters.getId()));
            }

            if (filters.getDescription() != null && !filters.getDescription().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")),
                        "%" + filters.getDescription().toLowerCase() + "%"));
            }

            if (filters.getAmountInPesos() != null) {
                predicates.add(cb.equal(root.get("amountInPesos"), filters.getAmountInPesos()));
            }

            if (filters.getAmountInDollars() != null) {
                predicates.add(cb.equal(root.get("amountInDollars"), filters.getAmountInDollars()));
            }

            if (filters.getSource() != null && filters.getSource().getId() != null) {
                predicates.add(cb.equal(root.get("source").get("id"), filters.getSource().getId()));
            }

            if (filters.getDate() != null) {
                predicates.add(cb.equal(root.get("date"), filters.getDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}