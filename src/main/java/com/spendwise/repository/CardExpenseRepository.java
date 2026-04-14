package com.spendwise.repository;

import com.spendwise.model.CardExpense;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardExpenseRepository extends JpaRepository<CardExpense, Long>, JpaSpecificationExecutor<CardExpense> {
    Optional<CardExpense> findByIdAndUser(Long id, User user);
    void deleteAllByUser(User user);
}
