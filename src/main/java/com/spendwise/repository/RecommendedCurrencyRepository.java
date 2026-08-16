package com.spendwise.repository;

import com.spendwise.model.RecommendedCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendedCurrencyRepository extends JpaRepository<RecommendedCurrency, Long> {
    List<RecommendedCurrency> findAllByOrderByDisplayOrderAsc();
}
