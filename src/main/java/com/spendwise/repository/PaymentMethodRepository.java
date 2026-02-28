package com.spendwise.repository;

import com.spendwise.model.PaymentMethod;
import com.spendwise.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long>, JpaSpecificationExecutor<PaymentMethod> {
    Optional<PaymentMethod> findByIdAndUser(Long id, User user);
}
