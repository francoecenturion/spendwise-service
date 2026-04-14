package com.spendwise.service;

import com.spendwise.dto.CurrencyDTO;
import com.spendwise.dto.RegisterWithSetupDTO;
import com.spendwise.enums.PaymentMethodType;
import com.spendwise.model.Category;
import com.spendwise.model.RecommendedCategory;
import com.spendwise.repository.RecommendedCategoryRepository;
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
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.CurrencyRepository;
import com.spendwise.repository.CardExpenseRepository;
import com.spendwise.repository.PersonalDebtRepository;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.GmailCredentialRepository;
import com.spendwise.repository.IncomeRepository;
import com.spendwise.repository.IssuingEntityRepository;
import com.spendwise.repository.MailImportRepository;
import com.spendwise.repository.MerchantBindingRepository;
import com.spendwise.repository.PasswordResetTokenRepository;
import com.spendwise.repository.PaymentMethodRepository;
import com.spendwise.repository.RecurrentExpenseRecordRepository;
import com.spendwise.repository.RecurrentExpenseRepository;
import com.spendwise.repository.RecommendedEntityRepository;
import com.spendwise.repository.RecommendedPaymentMethodRepository;
import com.spendwise.repository.SavingRepository;
import com.spendwise.repository.SavingsWalletRepository;
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
import java.util.Set;
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
    private final RecommendedCategoryRepository recommendedCategoryRepository;
    private final RefreshTokenService refreshTokenService;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final CardExpenseRepository cardExpenseRepository;
    private final PersonalDebtRepository personalDebtRepository;
    private final BudgetRepository budgetRepository;
    private final SavingRepository savingRepository;
    private final SavingsWalletRepository savingsWalletRepository;
    private final RecurrentExpenseRepository recurrentExpenseRepository;
    private final RecurrentExpenseRecordRepository recurrentExpenseRecordRepository;
    private final MailImportRepository mailImportRepository;
    private final MerchantBindingRepository merchantBindingRepository;
    private final GmailCredentialRepository gmailCredentialRepository;
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
                       CategoryRepository categoryRepository,
                       RecommendedCategoryRepository recommendedCategoryRepository,
                       RefreshTokenService refreshTokenService,
                       ExpenseRepository expenseRepository,
                       IncomeRepository incomeRepository,
                       CardExpenseRepository cardExpenseRepository,
                       PersonalDebtRepository personalDebtRepository,
                       BudgetRepository budgetRepository,
                       SavingRepository savingRepository,
                       SavingsWalletRepository savingsWalletRepository,
                       RecurrentExpenseRepository recurrentExpenseRepository,
                       RecurrentExpenseRecordRepository recurrentExpenseRecordRepository,
                       MailImportRepository mailImportRepository,
                       MerchantBindingRepository merchantBindingRepository,
                       GmailCredentialRepository gmailCredentialRepository) {
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
        this.recommendedCategoryRepository = recommendedCategoryRepository;
        this.refreshTokenService = refreshTokenService;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.cardExpenseRepository = cardExpenseRepository;
        this.personalDebtRepository = personalDebtRepository;
        this.budgetRepository = budgetRepository;
        this.savingRepository = savingRepository;
        this.savingsWalletRepository = savingsWalletRepository;
        this.recurrentExpenseRepository = recurrentExpenseRepository;
        this.recurrentExpenseRecordRepository = recurrentExpenseRecordRepository;
        this.mailImportRepository = mailImportRepository;
        this.merchantBindingRepository = merchantBindingRepository;
        this.gmailCredentialRepository = gmailCredentialRepository;
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
        // Always create generic Efectivo and Transferencia bancaria
        List<RecommendedPaymentMethod> defaultPms = recommendedPaymentMethodRepository
                .findByEntityIsNullAndPaymentMethodTypeIn(List.of(PaymentMethodType.CASH, PaymentMethodType.TRANSFER));
        Set<Long> defaultPmIds = new java.util.HashSet<>();
        for (RecommendedPaymentMethod rec : defaultPms) {
            PaymentMethod pm = new PaymentMethod();
            pm.setName(rec.getName());
            pm.setIcon(rec.getIconUrl());
            pm.setPaymentMethodType(rec.getPaymentMethodType());
            pm.setEnabled(true);
            pm.setUser(user);
            paymentMethodRepository.save(pm);
            defaultPmIds.add(rec.getId());
        }

        // Create user-selected payment methods (skip any already created above)
        List<Long> selectedPmIds = dto.getSelectedPaymentMethodIds();
        if (selectedPmIds != null) {
            for (Long pmId : selectedPmIds) {
                if (defaultPmIds.contains(pmId)) continue;
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

        // ── Create default categories from recommended list ───────────────────
        List<RecommendedCategory> recommendedCategories = recommendedCategoryRepository.findAllByOrderByDisplayOrderAsc();
        for (RecommendedCategory rec : recommendedCategories) {
            Category cat = new Category();
            cat.setName(rec.getName());
            cat.setIcon(rec.getIcon());
            cat.setType(rec.getType());
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
        String refreshToken = refreshTokenService.create(user).getToken();
        log.debug("User {} logged in successfully", user.getEmail());
        String roleName = user.getRole() != null ? user.getRole().name() : "USER";
        return new AuthResponseDTO(token, refreshToken, user.getEmail(), user.getName(),
                user.getSurname(), user.getProfilePicture(), roleName);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponseDTO refresh(String refreshToken) {
        var rt = refreshTokenService.verify(refreshToken);
        User user = rt.getUser();
        // Rotate: delete old, issue new
        refreshTokenService.delete(refreshToken);
        String newRefreshToken = refreshTokenService.create(user).getToken();
        String newAccessToken = jwtUtil.generateToken(user);
        String roleName = user.getRole() != null ? user.getRole().name() : "USER";
        return new AuthResponseDTO(newAccessToken, newRefreshToken, user.getEmail(), user.getName(),
                user.getSurname(), user.getProfilePicture(), roleName);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.delete(refreshToken);
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

    // ── Delete account ────────────────────────────────────────────────────────

    @Transactional
    @Override
    public void deleteAccount() {
        User user = userRepository.findById(currentUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Delete in FK-safe order
        recurrentExpenseRecordRepository.deleteAllByUser(user);
        mailImportRepository.deleteAllByUser(user);
        expenseRepository.deleteAllByUser(user);
        recurrentExpenseRepository.deleteAllByUser(user);
        incomeRepository.deleteAllByUser(user);
        cardExpenseRepository.deleteAllByUser(user);
        personalDebtRepository.deleteAllByUser(user);
        budgetRepository.deleteAllByUser(user);
        merchantBindingRepository.deleteAllByUser(user);
        savingRepository.deleteAllByUser(user);
        savingsWalletRepository.deleteAllByUser(user);
        paymentMethodRepository.deleteAllByUser(user);
        issuingEntityRepository.deleteAllByUser(user);
        categoryRepository.deleteAllByUser(user);
        currencyRepository.deleteAllByUser(user);
        gmailCredentialRepository.deleteByUser(user);
        passwordResetTokenRepository.deleteByUser(user);
        verificationTokenRepository.deleteByUser(user);
        refreshTokenService.deleteAllForUser(user);
        userRepository.delete(user);

        log.debug("Account permanently deleted for user {}", user.getEmail());
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
