package com.spendwise.repository;

import com.spendwise.enums.PaymentMethodType;
import com.spendwise.model.RecommendedPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendedPaymentMethodRepository extends JpaRepository<RecommendedPaymentMethod, Long> {
    List<RecommendedPaymentMethod> findAllByOrderByIdAsc();
    List<RecommendedPaymentMethod> findByEntityIsNullAndPaymentMethodTypeIn(List<PaymentMethodType> types);
}
