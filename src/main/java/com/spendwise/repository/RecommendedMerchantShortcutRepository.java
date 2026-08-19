package com.spendwise.repository;

import com.spendwise.model.RecommendedMerchantShortcut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendedMerchantShortcutRepository extends JpaRepository<RecommendedMerchantShortcut, Long> {
    List<RecommendedMerchantShortcut> findAllByOrderByDisplayOrderAsc();
}
