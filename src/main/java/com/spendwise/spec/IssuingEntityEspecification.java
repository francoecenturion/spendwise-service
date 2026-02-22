package com.spendwise.spec;

import com.spendwise.dto.IssuingEntityFilterDTO;
import com.spendwise.model.IssuingEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class IssuingEntityEspecification {

    public static Specification<IssuingEntity> withFilters(IssuingEntityFilterDTO filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.getDescription() != null && !filters.getDescription().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")),
                        "%" + filters.getDescription().toLowerCase() + "%"));
            }

            if (filters.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), filters.getEnabled()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
