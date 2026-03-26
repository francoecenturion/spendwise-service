package com.spendwise.repository;

import com.spendwise.model.Expense;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    Optional<Expense> findByIdAndUser(Long id, User user);

    @Query("SELECT year(e.date), SUM(e.amountInPesos), SUM(e.amountInDollars) FROM Expense e WHERE e.user = :user GROUP BY year(e.date) ORDER BY year(e.date) DESC")
    List<Object[]> getYearlySums(@Param("user") User user);
}
