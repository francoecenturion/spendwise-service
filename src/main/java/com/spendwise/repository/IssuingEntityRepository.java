package com.spendwise.repository;

import com.spendwise.model.IssuingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IssuingEntityRepository extends JpaRepository<IssuingEntity, Long>,
        JpaSpecificationExecutor<IssuingEntity> {
}
