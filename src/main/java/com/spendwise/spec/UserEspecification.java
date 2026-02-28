package com.spendwise.spec;

import com.spendwise.dto.UserFilterDTO;
import com.spendwise.model.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserEspecification {

    public static Specification<User> withFilters(UserFilterDTO filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.getName() != null && !filters.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + filters.getName().toLowerCase() + "%"));
            }

            if (filters.getSurname() != null && !filters.getSurname().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("surname")),
                        "%" + filters.getSurname().toLowerCase() + "%"));
            }

            if (filters.getEmail() != null && !filters.getEmail().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                        "%" + filters.getEmail().toLowerCase() + "%"));
            }

            if (filters.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), filters.getEnabled()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
