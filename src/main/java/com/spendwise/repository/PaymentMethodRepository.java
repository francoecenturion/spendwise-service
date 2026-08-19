package com.spendwise.repository;

import com.spendwise.model.IssuingEntity;
import com.spendwise.model.PaymentMethod;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long>, JpaSpecificationExecutor<PaymentMethod> {
    Optional<PaymentMethod> findByIdAndUser(Long id, User user);
    void deleteAllByUser(User user);
    List<PaymentMethod> findByIssuingEntity(IssuingEntity entity);

    @Modifying
    @Query("UPDATE PaymentMethod p SET p.isDefault = false WHERE p.user = :user AND p.id <> :excludeId")
    void clearDefaultsExcept(@Param("user") User user, @Param("excludeId") Long excludeId);
}
