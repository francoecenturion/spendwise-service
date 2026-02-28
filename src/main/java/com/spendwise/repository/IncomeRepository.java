package com.spendwise.repository;

import com.spendwise.model.Income;
import com.spendwise.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long>, JpaSpecificationExecutor<Income> {
    Optional<Income> findByIdAndUser(Long id, User user);
}
