package com.spendwise.spec;

import com.spendwise.dto.MailImportFilterDTO;
import com.spendwise.model.MailImport;
import com.spendwise.model.auth.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MailImportSpecification {

    public static Specification<MailImport> withFilters(MailImportFilterDTO filters, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            if (filters.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filters.getStatus()));
            }

            if (filters.getSenderEntity() != null && !filters.getSenderEntity().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("senderEntity")),
                        "%" + filters.getSenderEntity().toLowerCase() + "%"));
            }

            if (filters.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("parsedDate"), filters.getStartDate()));
            }

            if (filters.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("parsedDate"), filters.getEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
