package com.spendwise.repository;

import com.spendwise.model.MerchantShortcut;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantShortcutRepository extends JpaRepository<MerchantShortcut, Long>, JpaSpecificationExecutor<MerchantShortcut> {
    Optional<MerchantShortcut> findByIdAndUser(Long id, User user);
    void deleteAllByUser(User user);
}
