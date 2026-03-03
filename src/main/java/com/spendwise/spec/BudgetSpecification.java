package com.spendwise.spec;

import com.spendwise.dto.BudgetFilterDTO;
import com.spendwise.model.Budget;
import com.spendwise.model.auth.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BudgetSpecification {

    public static Specification<Budget> withFilters(BudgetFilterDTO filters, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            if (filters.getDescription() != null && !filters.getDescription().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")),
                        "%" + filters.getDescription().toLowerCase() + "%"));
            }

            if (filters.getMonth() != null) {
                predicates.add(cb.equal(root.get("month"), filters.getMonth()));
            }

            if (filters.getYear() != null) {
                predicates.add(cb.equal(root.get("year"), filters.getYear()));
            }

            if (filters.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), filters.getEnabled()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
