package com.spendwise.repository;

import com.spendwise.model.RecurrentExpense;
import com.spendwise.model.RecurrentExpenseRecord;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecurrentExpenseRecordRepository extends JpaRepository<RecurrentExpenseRecord, Long>, JpaSpecificationExecutor<RecurrentExpenseRecord> {

    Optional<RecurrentExpenseRecord> findByIdAndUser(Long id, User user);

    Optional<RecurrentExpenseRecord> findByRecurrentExpenseAndMonthAndYear(RecurrentExpense recurrentExpense, Integer month, Integer year);

}
