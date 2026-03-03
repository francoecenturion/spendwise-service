package com.spendwise.repository;

import com.spendwise.model.RecurrentExpense;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecurrentExpenseRepository extends JpaRepository<RecurrentExpense, Long>, JpaSpecificationExecutor<RecurrentExpense> {

    Optional<RecurrentExpense> findByIdAndUser(Long id, User user);

    Optional<RecurrentExpense> findByDescriptionIgnoreCaseAndUserAndEnabledTrue(String description, User user);

}
