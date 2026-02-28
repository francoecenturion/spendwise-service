package com.spendwise.unittest;

import com.spendwise.model.user.User;
import com.spendwise.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil Unit Tests")
public class JwtUtilTest {

    // At least 32 characters for HS256
    private static final String SECRET = "test-secret-key-at-least-32-characters-long!";
    private static final long EXPIRATION_MS = 86400000L; // 24h

    private JwtUtil jwtUtil;
    private User user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION_MS);

        user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setName("John");
        user.setSurname("Doe");
        user.setEnabled(true);
    }

    // ───────────────────────── generateToken ────────────────────

    @Test
    @DisplayName("generateToken returns a non-null, non-empty token string")
    public void testGenerateTokenNotNull() {
        String token = jwtUtil.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("generateToken produces a token with three JWT segments (header.payload.signature)")
    public void testGenerateTokenHasThreeParts() {
        String token = jwtUtil.generateToken(user);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "A JWT must have exactly 3 dot-separated parts");
    }

    // ───────────────────────── extractEmail ─────────────────────

    @Test
    @DisplayName("extractEmail returns the correct email from a generated token")
    public void testExtractEmail() {
        String token = jwtUtil.generateToken(user);
        String extractedEmail = jwtUtil.extractEmail(token);
        assertEquals("john@example.com", extractedEmail);
    }

    @Test
    @DisplayName("extractEmail returns correct email for different users")
    public void testExtractEmailDifferentUsers() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("jane@example.com");
        anotherUser.setName("Jane");
        anotherUser.setEnabled(true);

        String token1 = jwtUtil.generateToken(user);
        String token2 = jwtUtil.generateToken(anotherUser);

        assertEquals("john@example.com", jwtUtil.extractEmail(token1));
        assertEquals("jane@example.com", jwtUtil.extractEmail(token2));
    }

    // ───────────────────────── isTokenValid ─────────────────────

    @Test
    @DisplayName("isTokenValid returns true for a freshly generated token")
    public void testIsTokenValidWithValidToken() {
        String token = jwtUtil.generateToken(user);
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    @DisplayName("isTokenValid returns false for a tampered token")
    public void testIsTokenValidWithTamperedToken() {
        String token = jwtUtil.generateToken(user);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(jwtUtil.isTokenValid(tamperedToken));
    }

    @Test
    @DisplayName("isTokenValid returns false for a random string")
    public void testIsTokenValidWithRandomString() {
        assertFalse(jwtUtil.isTokenValid("this.is.not.a.jwt"));
    }

    @Test
    @DisplayName("isTokenValid returns false for an empty string")
    public void testIsTokenValidWithEmptyString() {
        assertFalse(jwtUtil.isTokenValid(""));
    }

    @Test
    @DisplayName("isTokenValid returns false for a token signed with a different secret")
    public void testIsTokenValidWithDifferentSecret() {
        JwtUtil otherJwt = new JwtUtil("completely-different-secret-key-at-least-32-chars", EXPIRATION_MS);
        String tokenFromOtherSecret = otherJwt.generateToken(user);
        assertFalse(jwtUtil.isTokenValid(tokenFromOtherSecret));
    }

    @Test
    @DisplayName("isTokenValid returns false for an already-expired token")
    public void testIsTokenValidWithExpiredToken() {
        JwtUtil shortLivedJwt = new JwtUtil(SECRET, -1L); // expiration in the past
        String expiredToken = shortLivedJwt.generateToken(user);
        assertFalse(shortLivedJwt.isTokenValid(expiredToken));
    }

    // ───────────────────────── round-trip ───────────────────────

    @Test
    @DisplayName("Token round-trip: generate → validate → extract email")
    public void testTokenRoundTrip() {
        String token = jwtUtil.generateToken(user);
        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals(user.getEmail(), jwtUtil.extractEmail(token));
    }
}
