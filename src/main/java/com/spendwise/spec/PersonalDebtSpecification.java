package com.spendwise.spec;

import com.spendwise.dto.PersonalDebtFilterDTO;
import com.spendwise.model.PersonalDebt;
import com.spendwise.model.auth.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PersonalDebtSpecification {

    public static Specification<PersonalDebt> withFilters(PersonalDebtFilterDTO filters, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            if (filters.getDescription() != null && !filters.getDescription().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")),
                        "%" + filters.getDescription().toLowerCase() + "%"));
            }
            if (filters.getCancelled() != null) {
                predicates.add(cb.equal(root.get("cancelled"), filters.getCancelled()));
            }
            if (filters.getCreditor() != null && !filters.getCreditor().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("creditor")),
                        "%" + filters.getCreditor().toLowerCase() + "%"));
            }
            if (filters.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), filters.getStartDate()));
            }
            if (filters.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), filters.getEndDate()));
            }
            if (filters.getMinAmountInPesos() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amountInPesos"), filters.getMinAmountInPesos()));
            }
            if (filters.getMaxAmountInPesos() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amountInPesos"), filters.getMaxAmountInPesos()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
