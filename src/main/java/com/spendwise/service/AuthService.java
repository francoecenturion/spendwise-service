package com.spendwise.service;

import com.spendwise.dto.UserDTO;
import com.spendwise.dto.auth.AuthResponseDTO;
import com.spendwise.dto.auth.LoginRequestDTO;
import com.spendwise.dto.auth.UpdateProfileDTO;
import com.spendwise.model.auth.VerificationToken;
import com.spendwise.model.user.User;
import com.spendwise.repository.UserRepository;
import com.spendwise.repository.VerificationTokenRepository;
import com.spendwise.security.JwtUtil;
import com.spendwise.service.interfaces.IAuthService;
import com.spendwise.service.interfaces.IEmailService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService implements IAuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final IEmailService emailService;
    private final UserService userService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public AuthService(UserRepository userRepository,
                       VerificationTokenRepository verificationTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       IEmailService emailService,
                       UserService userService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.userService = userService;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public String register(UserDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 8 caracteres");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        userService.populate(user, dto);
        user.setEnabled(false);
        userRepository.save(user);

        verificationTokenRepository.findByUser(user)
                .ifPresent(verificationTokenRepository::delete);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);

        String link = baseUrl + "/verify-email?token=" + verificationToken.getToken();
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), link);

        log.debug("User {} registered, verification email sent", user.getEmail());
        return "Registration successful. Please check your email to verify your account.";
    }

    // ── Verify email ──────────────────────────────────────────────────────────

    @Transactional
    @Override
    public String verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Verification token has expired. Please register again.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        log.debug("User {} verified successfully", user.getEmail());
        return "Email verified successfully. You can now log in.";
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Account not verified. Please check your email.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtUtil.generateToken(user);
        log.debug("User {} logged in successfully", user.getEmail());
        return new AuthResponseDTO(token, user.getEmail(), user.getName(),
                user.getSurname(), user.getProfilePicture());
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    @Override
    public UserDTO getProfile() {
        User user = currentUser();
        return modelMapper.map(user, UserDTO.class);
    }

    @Transactional
    @Override
    public UserDTO updateProfile(UpdateProfileDTO dto) {
        User user = userRepository.findById(currentUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getSurname() != null) user.setSurname(dto.getSurname());
        if (dto.getProfilePicture() != null) user.setProfilePicture(dto.getProfilePicture());

        User saved = userRepository.save(user);
        log.debug("Profile updated for user {}", saved.getEmail());
        return modelMapper.map(saved, UserDTO.class);
    }

    // ── Delete account (logical) ──────────────────────────────────────────────

    @Transactional
    @Override
    public void deleteAccount() {
        User user = userRepository.findById(currentUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setEnabled(false);
        userRepository.save(user);
        log.debug("Account disabled for user {}", user.getEmail());
    }
}
