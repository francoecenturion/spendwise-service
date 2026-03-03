package com.spendwise.spec;

import com.spendwise.dto.RecurrentExpenseRecordFilterDTO;
import com.spendwise.model.RecurrentExpenseRecord;
import com.spendwise.model.auth.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RecurrentExpenseRecordSpecification {

    public static Specification<RecurrentExpenseRecord> withFilters(RecurrentExpenseRecordFilterDTO filters, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            if (filters.getRecurrentExpenseId() != null) {
                predicates.add(cb.equal(root.get("recurrentExpense").get("id"), filters.getRecurrentExpenseId()));
            }

            if (filters.getMonth() != null) {
                predicates.add(cb.equal(root.get("month"), filters.getMonth()));
            }

            if (filters.getYear() != null) {
                predicates.add(cb.equal(root.get("year"), filters.getYear()));
            }

            if (filters.getCancelled() != null) {
                predicates.add(cb.equal(root.get("cancelled"), filters.getCancelled()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
