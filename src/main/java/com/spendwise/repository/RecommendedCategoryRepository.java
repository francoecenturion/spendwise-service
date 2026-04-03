package com.spendwise.repository;

import com.spendwise.model.RecommendedCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendedCategoryRepository extends JpaRepository<RecommendedCategory, Long> {
    List<RecommendedCategory> findAllByOrderByDisplayOrderAsc();
}
