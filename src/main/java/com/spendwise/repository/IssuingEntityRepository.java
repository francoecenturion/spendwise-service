package com.spendwise.repository;

import com.spendwise.model.IssuingEntity;
import com.spendwise.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IssuingEntityRepository extends JpaRepository<IssuingEntity, Long>,
        JpaSpecificationExecutor<IssuingEntity> {
    Optional<IssuingEntity> findByIdAndUser(Long id, User user);
}
