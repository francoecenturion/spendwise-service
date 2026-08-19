package com.spendwise.config;

import com.spendwise.enums.CategoryType;
import com.spendwise.enums.PaymentMethodType;
import com.spendwise.enums.Role;
import com.spendwise.model.Category;
import com.spendwise.model.Currency;
import com.spendwise.model.IssuingEntity;
import com.spendwise.model.MerchantShortcut;
import com.spendwise.model.PaymentMethod;
import com.spendwise.model.RecommendedCategory;
import com.spendwise.model.RecommendedCurrency;
import com.spendwise.model.RecommendedEntity;
import com.spendwise.model.RecommendedMerchantShortcut;
import com.spendwise.model.auth.User;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.CurrencyRepository;
import com.spendwise.repository.IssuingEntityRepository;
import com.spendwise.repository.MerchantShortcutRepository;
import com.spendwise.repository.PaymentMethodRepository;
import com.spendwise.repository.RecommendedCategoryRepository;
import com.spendwise.repository.RecommendedCurrencyRepository;
import com.spendwise.repository.RecommendedEntityRepository;
import com.spendwise.repository.RecommendedMerchantShortcutRepository;
import com.spendwise.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SetupDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(SetupDataInitializer.class);

    private final RecommendedEntityRepository entityRepo;
    private final RecommendedCurrencyRepository currencyRepo;
    private final RecommendedCategoryRepository categoryRepo;
    private final RecommendedMerchantShortcutRepository merchantShortcutRepo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository userCategoryRepo;
    private final CurrencyRepository userCurrencyRepo;
    private final IssuingEntityRepository userEntityRepo;
    private final PaymentMethodRepository userPmRepo;
    private final MerchantShortcutRepository userMerchantShortcutRepo;

    @Value("${admin.password:12345678}")
    private String adminPassword;

    @Value("${admin.email:admin@admin.com}")
    private String adminEmail;

    public SetupDataInitializer(RecommendedEntityRepository entityRepo,
                                RecommendedCurrencyRepository currencyRepo,
                                RecommendedCategoryRepository categoryRepo,
                                RecommendedMerchantShortcutRepository merchantShortcutRepo,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                CategoryRepository userCategoryRepo,
                                CurrencyRepository userCurrencyRepo,
                                IssuingEntityRepository userEntityRepo,
                                PaymentMethodRepository userPmRepo,
                                MerchantShortcutRepository userMerchantShortcutRepo) {
        this.entityRepo = entityRepo;
        this.currencyRepo = currencyRepo;
        this.categoryRepo = categoryRepo;
        this.merchantShortcutRepo = merchantShortcutRepo;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userCategoryRepo = userCategoryRepo;
        this.userCurrencyRepo = userCurrencyRepo;
        this.userEntityRepo = userEntityRepo;
        this.userPmRepo = userPmRepo;
        this.userMerchantShortcutRepo = userMerchantShortcutRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        seedCurrencies();
        seedCategories();
        seedMerchantShortcuts();
        seedEntities();
        // Runs last: clones the recommended data above into the admin's own account.
        seedAdminUser();
    }

    private void seedEntities() {
        if (entityRepo.count() > 0) return;

        log.info("Seeding recommended entities...");

        String[] entityNames = {
            "Santander", "Banco Galicia", "Banco Nación", "BBVA",
            "Banco Macro", "Banco Patagonia", "Brubank",
            "Naranja X", "MercadoPago", "Ualá", "Personal Pay"
        };

        for (String entityName : entityNames) {
            RecommendedEntity e = new RecommendedEntity();
            e.setName(entityName);
            entityRepo.save(e);
        }

        log.info("Seeded {} entities", entityNames.length);
    }

    private void seedCurrencies() {
        if (currencyRepo.count() > 0) return;

        Object[][] currencies = {
            { "Peso Argentino",        "$",   1, true  },
            { "Dólar Estadounidense",  "US$", 2, false },
            { "Real Brasileño",        "R$",  3, false },
        };

        for (Object[] row : currencies) {
            RecommendedCurrency c = new RecommendedCurrency();
            c.setName((String) row[0]);
            c.setSymbol((String) row[1]);
            c.setDisplayOrder((Integer) row[2]);
            c.setDefaultSelected((Boolean) row[3]);
            currencyRepo.save(c);
        }

        log.info("Seeded {} recommended currencies", currencies.length);
    }

    private void seedCategories() {
        if (categoryRepo.count() > 0) return;

        Object[][] defaultCategories = {
            { "Víveres",         "ShoppingCart",  CategoryType.EXPENSE,     1  },
            { "Restaurantes",    "Utensils",      CategoryType.EXPENSE,     2  },
            { "Transporte",      "Car",           CategoryType.EXPENSE,     3  },
            { "Hogar",           "Home",          CategoryType.EXPENSE,     4  },
            { "Servicios",       "Zap",           CategoryType.EXPENSE,     5  },
            { "Salud",           "Pill",          CategoryType.EXPENSE,     6  },
            { "Entretenimiento", "Gamepad2",      CategoryType.EXPENSE,     7  },
            { "Ropa",            "Shirt",         CategoryType.EXPENSE,     8  },
            { "Tecnología",      "Laptop",        CategoryType.EXPENSE,     9  },
            { "Educación",       "BookOpen",      CategoryType.EXPENSE,     10 },
            { "Café / Salidas",  "Coffee",        CategoryType.EXPENSE,     11 },
            { "Mascotas",        "PawPrint",      CategoryType.EXPENSE,     12 },
            { "Sueldo",          "Wallet",        CategoryType.INCOME,      13 },
            { "Freelance",       "Globe",         CategoryType.INCOME,      14 },
            { "Alquiler",        "Building2",     CategoryType.INCOME,      15 },
            { "Ahorro personal", "Star",          CategoryType.SAVING,      16 },
            { "Inversiones",     "TrendingUp",    CategoryType.INVESTMENT,  17 },
        };

        for (Object[] row : defaultCategories) {
            RecommendedCategory cat = new RecommendedCategory();
            cat.setName((String) row[0]);
            cat.setIcon((String) row[1]);
            cat.setType((CategoryType) row[2]);
            cat.setDisplayOrder((Integer) row[3]);
            categoryRepo.save(cat);
        }

        log.info("Seeded {} recommended categories", defaultCategories.length);
    }

    private void seedMerchantShortcuts() {
        if (merchantShortcutRepo.count() > 0) return;

        Map<String, RecommendedCategory> categoryByName = categoryRepo.findAllByOrderByDisplayOrderAsc()
                .stream()
                .collect(java.util.stream.Collectors.toMap(RecommendedCategory::getName, c -> c, (a, b) -> a));

        // { name, icon, categoryName }
        Object[][] shortcuts = {
            { "Uber",       "Car",         "Transporte" },
            { "Cabify",     "Car",         "Transporte" },
            { "Nafta",      "Fuel",        "Transporte" },
            { "Rappi",      "Pizza",       "Restaurantes" },
            { "PedidosYa",  "Pizza",       "Restaurantes" },
            { "Supermercado","ShoppingCart","Víveres" },
            { "Netflix",    "Tv",          "Entretenimiento" },
            { "Spotify",    "Music",       "Entretenimiento" },
            { "Farmacia",   "Pill",        "Salud" },
        };

        int displayOrder = 1;
        int seeded = 0;
        for (Object[] row : shortcuts) {
            RecommendedCategory category = categoryByName.get((String) row[2]);
            if (category == null) continue;
            RecommendedMerchantShortcut m = new RecommendedMerchantShortcut();
            m.setName((String) row[0]);
            m.setIcon((String) row[1]);
            m.setCategory(category);
            m.setDisplayOrder(displayOrder++);
            merchantShortcutRepo.save(m);
            seeded++;
        }

        log.info("Seeded {} recommended merchant shortcuts", seeded);
    }

    private void seedAdminUser() {
        if (userRepository.findByEmail(adminEmail).isPresent()) return;

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setName("Admin");
        admin.setSurname("SpendWise");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);
        log.info("Admin user created: {}", adminEmail);

        seedAdminDefaultData(admin);
    }

    /**
     * Clones every recommended currency/category/entity/payment method/merchant shortcut
     * into the admin's own account, mirroring what a full registration wizard run would
     * produce — so the admin account is immediately usable for manual testing.
     */
    private void seedAdminDefaultData(User admin) {
        // ── Currencies ────────────────────────────────────────────────────────
        for (RecommendedCurrency rec : currencyRepo.findAll()) {
            Currency currency = new Currency();
            currency.setName(rec.getName());
            currency.setSymbol(rec.getSymbol());
            currency.setEnabled(true);
            currency.setIsDefault(Boolean.TRUE.equals(rec.getDefaultSelected()));
            currency.setUser(admin);
            userCurrencyRepo.save(currency);
        }

        // ── Categories ────────────────────────────────────────────────────────
        Map<Long, Category> categoryMap = new LinkedHashMap<>();
        for (RecommendedCategory rec : categoryRepo.findAllByOrderByDisplayOrderAsc()) {
            Category cat = new Category();
            cat.setName(rec.getName());
            cat.setIcon(rec.getIcon());
            cat.setType(rec.getType());
            cat.setEnabled(true);
            cat.setUser(admin);
            categoryMap.put(rec.getId(), userCategoryRepo.save(cat));
        }

        // ── Issuing entities ──────────────────────────────────────────────────
        Map<Long, IssuingEntity> entityMap = new LinkedHashMap<>();
        for (RecommendedEntity rec : entityRepo.findAll()) {
            IssuingEntity ie = new IssuingEntity();
            ie.setDescription(rec.getName());
            ie.setIcon(rec.getIconUrl());
            ie.setEnabled(true);
            ie.setUser(admin);
            entityMap.put(rec.getId(), userEntityRepo.save(ie));
        }

        // ── Payment methods: one per type for each entity, plus a single Efectivo ──
        int paymentMethods = 0;
        for (IssuingEntity entity : entityMap.values()) {
            for (PaymentMethodType type : List.of(PaymentMethodType.QR, PaymentMethodType.TRANSFER,
                    PaymentMethodType.DEBIT_CARD, PaymentMethodType.CREDIT_CARD)) {
                PaymentMethod pm = new PaymentMethod();
                pm.setName(entity.getDescription() + " · " + paymentMethodTypeLabel(type));
                pm.setPaymentMethodType(type);
                pm.setEnabled(true);
                pm.setUser(admin);
                pm.setIssuingEntity(entity);
                userPmRepo.save(pm);
                paymentMethods++;
            }
        }
        PaymentMethod cash = new PaymentMethod();
        cash.setName("Efectivo");
        cash.setPaymentMethodType(PaymentMethodType.CASH);
        cash.setEnabled(true);
        cash.setUser(admin);
        userPmRepo.save(cash);
        paymentMethods++;

        // ── Merchant shortcuts ────────────────────────────────────────────────
        int shortcuts = 0;
        for (RecommendedMerchantShortcut rec : merchantShortcutRepo.findAllByOrderByDisplayOrderAsc()) {
            Category category = rec.getCategory() != null ? categoryMap.get(rec.getCategory().getId()) : null;
            if (category == null) continue;
            MerchantShortcut shortcut = new MerchantShortcut();
            shortcut.setName(rec.getName());
            shortcut.setIcon(rec.getIcon());
            shortcut.setCategory(category);
            shortcut.setEnabled(true);
            shortcut.setUser(admin);
            userMerchantShortcutRepo.save(shortcut);
            shortcuts++;
        }

        log.info("Seeded admin account with {} currencies, {} categories, {} entities, {} payment methods, {} merchant shortcuts",
                currencyRepo.count(), categoryMap.size(), entityMap.size(), paymentMethods, shortcuts);
    }

    private String paymentMethodTypeLabel(PaymentMethodType type) {
        return switch (type) {
            case QR -> "QR";
            case TRANSFER -> "Transferencia";
            case DEBIT_CARD -> "Débito";
            case CREDIT_CARD -> "Crédito";
            case CASH -> "Efectivo";
        };
    }

}
