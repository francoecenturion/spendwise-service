package com.spendwise.repository;

import com.spendwise.model.Debt;
import com.spendwise.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long>,
        JpaSpecificationExecutor<Debt> {
    Optional<Debt> findByIdAndUser(Long id, User user);
}
