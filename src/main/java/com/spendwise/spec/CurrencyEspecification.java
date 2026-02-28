package com.spendwise.spec;

import com.spendwise.dto.CurrencyFilterDTO;
import com.spendwise.model.Currency;
import com.spendwise.model.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CurrencyEspecification {

    public static Specification<Currency> withFilters(CurrencyFilterDTO filters, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            if (filters.getName() != null && !filters.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + filters.getName().toLowerCase() + "%"));
            }

            if (filters.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), filters.getEnabled()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
