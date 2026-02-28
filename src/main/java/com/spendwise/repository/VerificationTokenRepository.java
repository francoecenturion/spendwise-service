package com.spendwise.repository;

import com.spendwise.model.auth.VerificationToken;
import com.spendwise.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);

}
