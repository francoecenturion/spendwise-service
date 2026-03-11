package com.spendwise.config;

import com.spendwise.enums.PaymentMethodType;
import com.spendwise.enums.Role;
import com.spendwise.model.RecommendedCurrency;
import com.spendwise.model.RecommendedEntity;
import com.spendwise.model.RecommendedPaymentMethod;
import com.spendwise.model.auth.User;
import com.spendwise.repository.RecommendedCurrencyRepository;
import com.spendwise.repository.RecommendedEntityRepository;
import com.spendwise.repository.RecommendedPaymentMethodRepository;
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
import java.util.Map;

@Component
public class SetupDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(SetupDataInitializer.class);

    private final RecommendedEntityRepository entityRepo;
    private final RecommendedPaymentMethodRepository pmRepo;
    private final RecommendedCurrencyRepository currencyRepo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    public SetupDataInitializer(RecommendedEntityRepository entityRepo,
                                RecommendedPaymentMethodRepository pmRepo,
                                RecommendedCurrencyRepository currencyRepo,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.entityRepo = entityRepo;
        this.pmRepo = pmRepo;
        this.currencyRepo = currencyRepo;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        seedAdminUser();
        seedCurrencies();
        if (entityRepo.count() > 0) return;

        log.info("Seeding recommended entities and payment methods...");

        // ── Entities ──────────────────────────────────────────────────────────
        String[] entityNames = {
            "Santander", "Banco Galicia", "Banco Nación", "BBVA",
            "Banco Macro", "Banco Patagonia", "Brubank",
            "Naranja X", "MercadoPago", "Ualá", "Personal Pay"
        };

        Map<String, RecommendedEntity> entityMap = new LinkedHashMap<>();
        for (int i = 0; i < entityNames.length; i++) {
            RecommendedEntity e = new RecommendedEntity();
            e.setName(entityNames[i]);
            e.setDisplayOrder(i + 1);
            entityMap.put(entityNames[i], entityRepo.save(e));
        }

        // ── Payment methods ───────────────────────────────────────────────────
        // { name, type, entityName (null = generic) }
        Object[][] pmData = {
            { "Efectivo",                     PaymentMethodType.CASH,        null           },
            { "Transferencia bancaria",        PaymentMethodType.TRANSFER,    null           },
            { "Santander Visa Débito",         PaymentMethodType.DEBIT_CARD,  "Santander"    },
            { "Santander Visa Crédito",        PaymentMethodType.CREDIT_CARD, "Santander"    },
            { "Santander Mastercard",          PaymentMethodType.CREDIT_CARD, "Santander"    },
            { "Galicia Visa Débito",           PaymentMethodType.DEBIT_CARD,  "Banco Galicia"},
            { "Galicia Visa Crédito",          PaymentMethodType.CREDIT_CARD, "Banco Galicia"},
            { "Galicia Mastercard",            PaymentMethodType.CREDIT_CARD, "Banco Galicia"},
            { "BNA Visa Débito",               PaymentMethodType.DEBIT_CARD,  "Banco Nación" },
            { "BNA Mastercard",                PaymentMethodType.CREDIT_CARD, "Banco Nación" },
            { "BBVA Visa Débito",              PaymentMethodType.DEBIT_CARD,  "BBVA"         },
            { "BBVA Visa Crédito",             PaymentMethodType.CREDIT_CARD, "BBVA"         },
            { "BBVA Mastercard",               PaymentMethodType.CREDIT_CARD, "BBVA"         },
            { "Macro Visa Débito",             PaymentMethodType.DEBIT_CARD,  "Banco Macro"  },
            { "Macro Visa Crédito",            PaymentMethodType.CREDIT_CARD, "Banco Macro"  },
            { "Macro Mastercard",              PaymentMethodType.CREDIT_CARD, "Banco Macro"  },
            { "Patagonia Visa Débito",         PaymentMethodType.DEBIT_CARD,  "Banco Patagonia"},
            { "Patagonia Visa Crédito",        PaymentMethodType.CREDIT_CARD, "Banco Patagonia"},
            { "Brubank Visa Débito",           PaymentMethodType.DEBIT_CARD,  "Brubank"      },
            { "Naranja X Visa",                PaymentMethodType.CREDIT_CARD, "Naranja X"    },
            { "Naranja X Mastercard",          PaymentMethodType.CREDIT_CARD, "Naranja X"    },
            { "Mercado Pago",                  PaymentMethodType.TRANSFER,    "MercadoPago"  },
            { "Ualá Mastercard",               PaymentMethodType.CREDIT_CARD, "Ualá"         },
            { "Ualá Visa Débito",              PaymentMethodType.DEBIT_CARD,  "Ualá"         },
            { "Personal Pay Visa",             PaymentMethodType.CREDIT_CARD, "Personal Pay" },
            { "Personal Pay Débito",           PaymentMethodType.DEBIT_CARD,  "Personal Pay" },
        };

        for (int i = 0; i < pmData.length; i++) {
            RecommendedPaymentMethod pm = new RecommendedPaymentMethod();
            pm.setName((String) pmData[i][0]);
            pm.setPaymentMethodType((PaymentMethodType) pmData[i][1]);
            pm.setDisplayOrder(i + 1);
            String entityName = (String) pmData[i][2];
            if (entityName != null) pm.setEntity(entityMap.get(entityName));
            pmRepo.save(pm);
        }

        log.info("Seeded {} entities and {} payment methods", entityMap.size(), pmData.length);
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

    private void seedAdminUser() {
        String adminEmail = "admin@spendwise.com";
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
    }

}
