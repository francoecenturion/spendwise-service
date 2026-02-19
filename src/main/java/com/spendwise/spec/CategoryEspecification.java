package com.spendwise.spec;

import com.spendwise.dto.CategoryFilterDTO;
import com.spendwise.model.Category;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CategoryEspecification {

    public static Specification<Category> withFilters(CategoryFilterDTO filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.getName() != null && !filters.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")),
                        "%" + filters.getName().toLowerCase() + "%"));
            }

            if (filters.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), filters.getEnabled()));
            }

            if (filters.getIsIncome() != null) {
                predicates.add(cb.equal(root.get("isIncome"), filters.getIsIncome()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
