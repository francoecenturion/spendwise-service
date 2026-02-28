package com.spendwise.repository;

import com.spendwise.model.SavingsWallet;
import com.spendwise.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavingsWalletRepository extends JpaRepository<SavingsWallet, Long>, JpaSpecificationExecutor<SavingsWallet> {
    Optional<SavingsWallet> findByIdAndUser(Long id, User user);
}
