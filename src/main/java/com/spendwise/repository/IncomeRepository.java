package com.spendwise.repository;

import com.spendwise.model.Income;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long>, JpaSpecificationExecutor<Income> {
    Optional<Income> findByIdAndUser(Long id, User user);

    @Query("SELECT year(i.date), SUM(i.amountInPesos), SUM(i.amountInDollars) FROM Income i WHERE i.user = :user GROUP BY year(i.date) ORDER BY year(i.date) DESC")
    List<Object[]> getYearlySums(@Param("user") User user);

    @Query("SELECT year(i.date), month(i.date), SUM(i.amountInPesos), SUM(i.amountInDollars) FROM Income i WHERE i.user = :user GROUP BY year(i.date), month(i.date) ORDER BY year(i.date) DESC, month(i.date) ASC")
    List<Object[]> getMonthlySums(@Param("user") User user);
}
