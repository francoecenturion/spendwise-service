package com.spendwise.repository;

import com.spendwise.model.PersonalDebt;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalDebtRepository extends JpaRepository<PersonalDebt, Long>, JpaSpecificationExecutor<PersonalDebt> {
    Optional<PersonalDebt> findByIdAndUser(Long id, User user);
    void deleteAllByUser(User user);
}
