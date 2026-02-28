package com.spendwise.spec;

import com.spendwise.dto.PaymentMethodFilterDTO;
import com.spendwise.enums.PaymentMethodType;
import com.spendwise.model.PaymentMethod;
import com.spendwise.model.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodEspecification {

    public static Specification<PaymentMethod> withFilters(PaymentMethodFilterDTO filters, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            // Corregido: buscar por "name" en lugar de "description"
            if (filters.getName() != null && !filters.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + filters.getName().toLowerCase() + "%"));
            }

            if (filters.getPaymentMethodType() != null && !filters.getPaymentMethodType().isEmpty()) {
                try {
                    PaymentMethodType type = PaymentMethodType.valueOf(filters.getPaymentMethodType());
                    predicates.add(cb.equal(root.get("paymentMethodType"), type));
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