package com.spendwise.repository;

import com.spendwise.model.RecommendedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendedEntityRepository extends JpaRepository<RecommendedEntity, Long> {
    List<RecommendedEntity> findAllByOrderByIdAsc();
}
