package com.spendwise.service;

import com.spendwise.dto.CurrencyDTO;
import com.spendwise.dto.RegisterWithSetupDTO;
import com.spendwise.enums.CategoryType;
import com.spendwise.model.Category;
import com.spendwise.dto.UserDTO;
import com.spendwise.dto.auth.AuthResponseDTO;
import com.spendwise.dto.auth.LoginRequestDTO;
import com.spendwise.dto.auth.UpdateProfileDTO;
import com.spendwise.model.Currency;
import com.spendwise.model.IssuingEntity;
import com.spendwise.model.PaymentMethod;
import com.spendwise.model.RecommendedEntity;
import com.spendwise.model.RecommendedPaymentMethod;
import com.spendwise.model.auth.VerificationToken;
import com.spendwise.model.auth.User;
import com.spendwise.model.auth.PasswordResetToken;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.CurrencyRepository;
import com.spendwise.repository.IssuingEntityRepository;
import com.spendwise.repository.PasswordResetTokenRepository;
import com.spendwise.repository.PaymentMethodRepository;
import com.spendwise.repository.RecommendedEntityRepository;
import com.spendwise.repository.RecommendedPaymentMethodRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final CurrencyRepository currencyRepository;
    private final IssuingEntityRepository issuingEntityRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final RecommendedEntityRepository recommendedEntityRepository;
    private final RecommendedPaymentMethodRepository recommendedPaymentMethodRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public AuthService(UserRepository userRepository,
                       VerificationTokenRepository verificationTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       IEmailService emailService,
                       UserService userService,
                       CurrencyRepository currencyRepository,
                       IssuingEntityRepository issuingEntityRepository,
                       PaymentMethodRepository paymentMethodRepository,
                       RecommendedEntityRepository recommendedEntityRepository,
                       RecommendedPaymentMethodRepository recommendedPaymentMethodRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.userService = userService;
        this.currencyRepository = currencyRepository;
        this.issuingEntityRepository = issuingEntityRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.recommendedEntityRepository = recommendedEntityRepository;
        this.recommendedPaymentMethodRepository = recommendedPaymentMethodRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.categoryRepository = categoryRepository;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public String register(RegisterWithSetupDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 8 caracteres");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        // ── Create user ───────────────────────────────────────────────────────
        User user = new User();
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(false);
        userRepository.save(user);

        // ── Create currencies ─────────────────────────────────────────────────
        List<CurrencyDTO> currencies = dto.getCurrencies();
        if (currencies != null && !currencies.isEmpty()) {
            for (int i = 0; i < currencies.size(); i++) {
                CurrencyDTO c = currencies.get(i);
                Currency currency = new Currency();
                currency.setName(c.getName());
                currency.setSymbol(c.getSymbol());
                currency.setEnabled(true);
                currency.setIsDefault(i == 0);
                currency.setUser(user);
                currencyRepository.save(currency);
            }
        }

        // ── Create issuing entities ───────────────────────────────────────────
        Map<Long, IssuingEntity> entityMap = new HashMap<>();
        List<Long> selectedEntityIds = dto.getSelectedEntityIds();
        if (selectedEntityIds != null) {
            for (Long entityId : selectedEntityIds) {
                recommendedEntityRepository.findById(entityId).ifPresent(rec -> {
                    IssuingEntity ie = new IssuingEntity();
                    ie.setDescription(rec.getName());
                    ie.setIcon(rec.getIconUrl());
                    ie.setEnabled(true);
                    ie.setUser(user);
                    entityMap.put(rec.getId(), issuingEntityRepository.save(ie));
                });
            }
        }

        // ── Create payment methods ────────────────────────────────────────────
        List<Long> selectedPmIds = dto.getSelectedPaymentMethodIds();
        if (selectedPmIds != null) {
            for (Long pmId : selectedPmIds) {
                recommendedPaymentMethodRepository.findById(pmId).ifPresent(rec -> {
                    PaymentMethod pm = new PaymentMethod();
                    pm.setName(rec.getName());
                    pm.setIcon(rec.getIconUrl());
                    pm.setPaymentMethodType(rec.getPaymentMethodType());
                    pm.setEnabled(true);
                    pm.setUser(user);
                    if (rec.getEntity() != null) {
                        pm.setIssuingEntity(entityMap.get(rec.getEntity().getId()));
                    }
                    paymentMethodRepository.save(pm);
                });
            }
        }

        // ── Create default categories ─────────────────────────────────────────
        Object[][] defaultCategories = {
            { "Víveres",         "ShoppingCart",  CategoryType.EXPENSE     },
            { "Restaurantes",    "Utensils",      CategoryType.EXPENSE     },
            { "Transporte",      "Car",           CategoryType.EXPENSE     },
            { "Hogar",           "Home",          CategoryType.EXPENSE     },
            { "Servicios",       "Zap",           CategoryType.EXPENSE     },
            { "Salud",           "Pill",          CategoryType.EXPENSE     },
            { "Entretenimiento", "Gamepad2",      CategoryType.EXPENSE     },
            { "Ropa",            "Shirt",         CategoryType.EXPENSE     },
            { "Tecnología",      "Laptop",        CategoryType.EXPENSE     },
            { "Educación",       "BookOpen",      CategoryType.EXPENSE     },
            { "Café / Salidas",  "Coffee",        CategoryType.EXPENSE     },
            { "Mascotas",        "PawPrint",      CategoryType.EXPENSE     },
            { "Sueldo",          "Wallet",        CategoryType.INCOME      },
            { "Freelance",       "Globe",         CategoryType.INCOME      },
            { "Alquiler",        "Building2",     CategoryType.INCOME      },
            { "Ahorro personal", "Star",          CategoryType.SAVING      },
            { "Inversiones",     "TrendingUp",    CategoryType.INVESTMENT  },
        };
        for (Object[] row : defaultCategories) {
            Category cat = new Category();
            cat.setName((String) row[0]);
            cat.setIcon((String) row[1]);
            cat.setType((CategoryType) row[2]);
            cat.setEnabled(true);
            cat.setUser(user);
            categoryRepository.save(cat);
        }

        // ── Send verification email ───────────────────────────────────────────
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);

        String link = baseUrl + "/verify-email?token=" + verificationToken.getToken();
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), link);

        log.debug("User {} registered with {} currencies, {} entities, {} payment methods",
                user.getEmail(),
                currencies != null ? currencies.size() : 0,
                entityMap.size(),
                selectedPmIds != null ? selectedPmIds.size() : 0);
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
        String roleName = user.getRole() != null ? user.getRole().name() : "USER";
        return new AuthResponseDTO(token, user.getEmail(), user.getName(),
                user.getSurname(), user.getProfilePicture(), roleName);
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

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            if (dto.getCurrentPassword() == null ||
                    !passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña actual incorrecta");
            }
            user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        }

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

    // ── Forgot password ───────────────────────────────────────────────────────

    @Transactional
    @Override
    public void forgotPassword(String email) {
        // Always return silently to avoid user enumeration
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser(user);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
            passwordResetTokenRepository.save(resetToken);

            String link = baseUrl + "/reset-password?token=" + resetToken.getToken();
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), link);
            log.debug("Password reset email requested for {}", email);
        });
    }

    // ── Reset password ────────────────────────────────────────────────────────

    @Transactional
    @Override
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 8 caracteres");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido o expirado"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace expiró. Solicitá uno nuevo.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
        log.debug("Password reset successfully for user {}", user.getEmail());
    }
}
