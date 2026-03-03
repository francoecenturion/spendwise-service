package com.spendwise.spec;

import com.spendwise.dto.RecurrentExpenseFilterDTO;
import com.spendwise.model.RecurrentExpense;
import com.spendwise.model.auth.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RecurrentExpenseSpecification {

    public static Specification<RecurrentExpense> withFilters(RecurrentExpenseFilterDTO filters, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            if (filters.getDescription() != null && !filters.getDescription().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")),
                        "%" + filters.getDescription().toLowerCase() + "%"));
            }

            if (filters.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filters.getCategoryId()));
            }

            if (filters.getPaymentMethodId() != null) {
                predicates.add(cb.equal(root.get("paymentMethod").get("id"), filters.getPaymentMethodId()));
            }

            if (filters.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), filters.getEnabled()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
