package com.spendwise.repository;

import com.spendwise.model.Saving;
import com.spendwise.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavingRepository extends JpaRepository<Saving, Long>, JpaSpecificationExecutor<Saving> {
    Optional<Saving> findByIdAndUser(Long id, User user);
}
