package com.spendwise.repository;

import com.spendwise.model.MerchantBinding;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantBindingRepository extends JpaRepository<MerchantBinding, Long> {
    Optional<MerchantBinding> findByUserAndMerchantNameIgnoreCase(User user, String merchantName);
}
