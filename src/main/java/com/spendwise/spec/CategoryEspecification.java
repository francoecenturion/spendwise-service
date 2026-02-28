package com.spendwise.spec;

import com.spendwise.dto.CategoryFilterDTO;
import com.spendwise.model.Category;
import com.spendwise.model.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CategoryEspecification {

    public static Specification<Category> withFilters(CategoryFilterDTO filters, User user) {
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

            if (filters.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filters.getType()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
