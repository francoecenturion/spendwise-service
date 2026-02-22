package com.spendwise.repository;

import com.spendwise.model.SavingsWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingsWalletRepository extends JpaRepository<SavingsWallet, Long>, JpaSpecificationExecutor<SavingsWallet> {
}
