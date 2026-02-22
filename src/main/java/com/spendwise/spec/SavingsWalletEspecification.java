package com.spendwise.spec;

import com.spendwise.dto.SavingsWalletFilterDTO;
import com.spendwise.enums.SavingsWalletType;
import com.spendwise.model.SavingsWallet;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SavingsWalletEspecification {

    public static Specification<SavingsWallet> withFilters(SavingsWalletFilterDTO filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.getName() != null && !filters.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + filters.getName().toLowerCase() + "%"));
            }

            if (filters.getSavingsWalletType() != null && !filters.getSavingsWalletType().isEmpty()) {
                try {
                    SavingsWalletType type = SavingsWalletType.valueOf(filters.getSavingsWalletType());
                    predicates.add(cb.equal(root.get("savingsWalletType"), type));
                } catch (IllegalArgumentException e) {
                    // Si el tipo es inválido, ignorar este filtro
                }
            }

            if (filters.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), filters.getEnabled()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
