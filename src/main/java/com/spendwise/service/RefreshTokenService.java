package com.spendwise.service;

import com.spendwise.model.auth.RefreshToken;
import com.spendwise.model.auth.User;
import com.spendwise.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration-ms:7776000000}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken create(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        return refreshTokenRepository.save(rt);
    }

    public RefreshToken verify(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(rt);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        return rt;
    }

    public void delete(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }

    public void deleteAllForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
