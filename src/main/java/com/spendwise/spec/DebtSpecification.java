package com.spendwise.spec;

import com.spendwise.dto.DebtFilterDTO;
import com.spendwise.model.Debt;
import com.spendwise.model.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DebtSpecification {

    public static Specification<Debt> withFilters(DebtFilterDTO filters, User user) {
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

            if (filters.getPersonal() != null) {
                predicates.add(cb.equal(root.get("personal"), filters.getPersonal()));
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

            if (filters.getPaymentMethodId() != null) {
                predicates.add(cb.equal(root.get("paymentMethod").get("id"), filters.getPaymentMethodId()));
            }

            if (filters.getIssuingEntityId() != null) {
                predicates.add(cb.equal(root.get("issuingEntity").get("id"), filters.getIssuingEntityId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
