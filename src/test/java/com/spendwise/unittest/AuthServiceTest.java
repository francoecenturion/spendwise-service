package com.spendwise.unittest;

import com.spendwise.dto.UserDTO;
import com.spendwise.dto.auth.AuthResponseDTO;
import com.spendwise.dto.auth.LoginRequestDTO;
import com.spendwise.dto.auth.UpdateProfileDTO;
import com.spendwise.model.auth.VerificationToken;
import com.spendwise.model.user.User;
import com.spendwise.repository.UserRepository;
import com.spendwise.repository.VerificationTokenRepository;
import com.spendwise.security.JwtUtil;
import com.spendwise.service.AuthService;
import com.spendwise.service.UserService;
import com.spendwise.service.interfaces.IEmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Unit Tests")
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private IEmailService emailService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "baseUrl", "http://localhost:8080");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ───────────────────────── helpers ──────────────────────────

    private void setSecurityContext(User user) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private User buildUser(Long id, String email, String name, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash("$2a$10$hashedPassword");
        user.setEnabled(enabled);
        return user;
    }

    private VerificationToken buildToken(User user, LocalDateTime expiryDate) {
        VerificationToken token = new VerificationToken();
        token.setToken("test-uuid-token");
        token.setUser(user);
        token.setExpiryDate(expiryDate);
        return token;
    }

    // ───────────────────────── register ─────────────────────────

    @Test
    @DisplayName("Register creates user with enabled=false, saves token and sends email")
    public void testRegister() {

        // Arrange
        UserDTO dto = new UserDTO();
        dto.setEmail("john@example.com");
        dto.setName("John");
        dto.setPassword("rawPassword");

        Mockito.when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

        // Act
        String result = authService.register(dto);

        // Assert
        assertEquals("Registration successful. Please check your email to verify your account.", result);
        Mockito.verify(userRepository).existsByEmail("john@example.com");
        Mockito.verify(userService).populate(any(User.class), eq(dto));
        Mockito.verify(userRepository).save(any(User.class));
        Mockito.verify(verificationTokenRepository).save(any(VerificationToken.class));
        Mockito.verify(emailService).sendVerificationEmail(any(), any(), contains("/auth/verify?token="));
    }

    @Test
    @DisplayName("Register throws CONFLICT when email is already in use")
    public void testRegisterEmailAlreadyInUse() {

        // Arrange
        UserDTO dto = new UserDTO();
        dto.setEmail("existing@example.com");
        dto.setName("John");
        dto.setPassword("rawPassword");

        Mockito.when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(dto));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        Mockito.verify(userRepository).existsByEmail("existing@example.com");
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoInteractions(userService, verificationTokenRepository, emailService);
    }

    @Test
    @DisplayName("Register sets enabled=false on the new user")
    public void testRegisterSetsEnabledFalse() {

        // Arrange
        UserDTO dto = new UserDTO();
        dto.setEmail("john@example.com");
        dto.setName("John");
        dto.setPassword("rawPassword");

        Mockito.when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Capture the user passed to save to verify enabled=false
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            assertFalse(saved.getEnabled(), "User should be disabled on registration");
            return saved;
        });

        // Act
        authService.register(dto);

        // Assert
        Mockito.verify(userRepository).save(any(User.class));
    }

    // ───────────────────────── verifyEmail ──────────────────────

    @Test
    @DisplayName("Verify email with valid token enables user and deletes token")
    public void testVerifyEmail() {

        // Arrange
        User user = buildUser(1L, "john@example.com", "John", false);
        VerificationToken token = buildToken(user, LocalDateTime.now().plusHours(12));

        Mockito.when(verificationTokenRepository.findByToken("test-uuid-token"))
                .thenReturn(Optional.of(token));
        Mockito.when(userRepository.save(user)).thenReturn(user);

        // Act
        String result = authService.verifyEmail("test-uuid-token");

        // Assert
        assertEquals("Email verified successfully. You can now log in.", result);
        assertTrue(user.getEnabled());
        Mockito.verify(verificationTokenRepository).findByToken("test-uuid-token");
        Mockito.verify(userRepository).save(user);
        Mockito.verify(verificationTokenRepository).delete(token);
    }

    @Test
    @DisplayName("Verify email with non-existing token throws NOT_FOUND")
    public void testVerifyEmailInvalidToken() {

        // Arrange
        Mockito.when(verificationTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.verifyEmail("invalid-token"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        Mockito.verify(verificationTokenRepository).findByToken("invalid-token");
        Mockito.verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Verify email with expired token throws BAD_REQUEST and deletes the token")
    public void testVerifyEmailExpiredToken() {

        // Arrange
        User user = buildUser(1L, "john@example.com", "John", false);
        VerificationToken expiredToken = buildToken(user, LocalDateTime.now().minusHours(1));

        Mockito.when(verificationTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.verifyEmail("expired-token"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        Mockito.verify(verificationTokenRepository).delete(expiredToken);
        Mockito.verifyNoInteractions(userRepository);
    }

    // ───────────────────────── login ────────────────────────────

    @Test
    @DisplayName("Login with valid credentials returns AuthResponseDTO with JWT")
    public void testLogin() {

        // Arrange
        User user = buildUser(1L, "john@example.com", "John", true);

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("john@example.com");
        dto.setPassword("rawPassword");

        Mockito.when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("rawPassword", "$2a$10$hashedPassword")).thenReturn(true);
        Mockito.when(jwtUtil.generateToken(user)).thenReturn("mocked.jwt.token");

        // Act
        AuthResponseDTO result = authService.login(dto);

        // Assert
        assertEquals("mocked.jwt.token", result.getToken());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("John", result.getName());
        Mockito.verify(userRepository).findByEmail("john@example.com");
        Mockito.verify(passwordEncoder).matches("rawPassword", "$2a$10$hashedPassword");
        Mockito.verify(jwtUtil).generateToken(user);
    }

    @Test
    @DisplayName("Login with non-existing email throws UNAUTHORIZED")
    public void testLoginEmailNotFound() {

        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("noone@example.com");
        dto.setPassword("password");

        Mockito.when(userRepository.findByEmail("noone@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(dto));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        Mockito.verify(userRepository).findByEmail("noone@example.com");
        Mockito.verifyNoInteractions(passwordEncoder, jwtUtil);
    }

    @Test
    @DisplayName("Login with unverified account throws FORBIDDEN")
    public void testLoginNotVerified() {

        // Arrange
        User user = buildUser(1L, "john@example.com", "John", false); // enabled=false

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("john@example.com");
        dto.setPassword("rawPassword");

        Mockito.when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(dto));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        Mockito.verifyNoInteractions(passwordEncoder, jwtUtil);
    }

    @Test
    @DisplayName("Login with wrong password throws UNAUTHORIZED")
    public void testLoginWrongPassword() {

        // Arrange
        User user = buildUser(1L, "john@example.com", "John", true);

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("john@example.com");
        dto.setPassword("wrongPassword");

        Mockito.when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(dto));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        Mockito.verify(passwordEncoder).matches("wrongPassword", "$2a$10$hashedPassword");
        Mockito.verifyNoInteractions(jwtUtil);
    }

    // ───────────────────────── getProfile ────────────────────────────────────

    @Test
    @DisplayName("getProfile returns UserDTO of the authenticated user without hitting the DB")
    public void testGetProfile() {

        // Arrange
        User user = buildUser(1L, "john@example.com", "John", true);
        user.setSurname("Doe");
        user.setProfilePicture("https://example.com/pic.jpg");
        setSecurityContext(user);

        // Act
        UserDTO result = authService.getProfile();

        // Assert
        assertEquals("john@example.com", result.getEmail());
        assertEquals("John", result.getName());
        assertEquals("Doe", result.getSurname());
        assertEquals("https://example.com/pic.jpg", result.getProfilePicture());
        Mockito.verifyNoInteractions(userRepository);
    }

    // ───────────────────────── updateProfile ─────────────────────────────────

    @Test
    @DisplayName("updateProfile updates all provided fields and returns updated UserDTO")
    public void testUpdateProfile() {

        // Arrange
        User user = buildUser(1L, "john@example.com", "John", true);
        user.setSurname("OldSurname");
        setSecurityContext(user);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setName("NewName");
        dto.setSurname("NewSurname");
        dto.setProfilePicture("https://cloudinary.com/pic.jpg");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);

        // Act
        UserDTO result = authService.updateProfile(dto);

        // Assert
        assertEquals("NewName", result.getName());
        assertEquals("NewSurname", result.getSurname());
        assertEquals("https://cloudinary.com/pic.jpg", result.getProfilePicture());
        Mockito.verify(userRepository).findById(1L);
        Mockito.verify(userRepository).save(user);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("updateProfile with null fields leaves those fields unchanged")
    public void testUpdateProfile_partialFields() {

        // Arrange
        User user = buildUser(1L, "john@example.com", "John", true);
        user.setSurname("OriginalSurname");
        user.setProfilePicture("https://existing.com/pic.jpg");
        setSecurityContext(user);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setName("UpdatedName");
        // surname and profilePicture are null → must not be overwritten

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);

        // Act
        UserDTO result = authService.updateProfile(dto);

        // Assert
        assertEquals("UpdatedName", result.getName());
        assertEquals("OriginalSurname", result.getSurname());
        assertEquals("https://existing.com/pic.jpg", result.getProfilePicture());
        Mockito.verify(userRepository).findById(1L);
        Mockito.verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateProfile throws NOT_FOUND when user does not exist in DB")
    public void testUpdateProfile_userNotFound() {

        // Arrange
        User user = buildUser(1L, "ghost@example.com", "Ghost", true);
        setSecurityContext(user);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setName("Whatever");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.updateProfile(dto));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        Mockito.verify(userRepository).findById(1L);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    // ───────────────────────── deleteAccount ─────────────────────────────────

    @Test
    @DisplayName("deleteAccount sets enabled=false and saves the user")
    public void testDeleteAccount() {

        // Arrange
        User user = buildUser(1L, "john@example.com", "John", true);
        setSecurityContext(user);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);

        // Act
        authService.deleteAccount();

        // Assert
        assertFalse(user.getEnabled(), "Account should be disabled after deletion");
        Mockito.verify(userRepository).findById(1L);
        Mockito.verify(userRepository).save(user);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("deleteAccount throws NOT_FOUND when user does not exist in DB")
    public void testDeleteAccount_userNotFound() {

        // Arrange
        User user = buildUser(1L, "ghost@example.com", "Ghost", true);
        setSecurityContext(user);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.deleteAccount());

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        Mockito.verify(userRepository).findById(1L);
        Mockito.verifyNoMoreInteractions(userRepository);
    }
}
