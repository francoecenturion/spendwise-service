package com.spendwise.repository;

import com.spendwise.model.Budget;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {

    Optional<Budget> findByIdAndUser(Long id, User user);

    Optional<Budget> findTopByUserOrderByYearDescMonthDesc(User user);

}
