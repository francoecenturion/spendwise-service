package com.spendwise.repository;

import com.spendwise.model.Currency;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long>, JpaSpecificationExecutor<Currency> {
    Optional<Currency> findByIdAndUser(Long id, User user);

    @Modifying
    @Query("UPDATE Currency c SET c.isDefault = false WHERE c.user = :user AND c.id <> :excludeId")
    void clearDefaultsExcept(@Param("user") User user, @Param("excludeId") Long excludeId);
}
