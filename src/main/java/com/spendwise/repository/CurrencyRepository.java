package com.spendwise.repository;

import com.spendwise.model.Currency;
import com.spendwise.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long>, JpaSpecificationExecutor<Currency> {
    Optional<Currency> findByIdAndUser(Long id, User user);
}
