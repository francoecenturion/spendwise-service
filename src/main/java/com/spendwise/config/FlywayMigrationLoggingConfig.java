package com.spendwise.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class FlywayMigrationLoggingConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayMigrationLoggingConfig.class);

    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            LOGGER.info("Starting Flyway migrations for schema 'spendwise'");

            try {
                flyway.migrate();
                LOGGER.info("Flyway migrations completed successfully");
            } catch (RuntimeException exception) {
                Throwable rootCause = findRootCause(exception);

                LOGGER.error(
                        "Flyway migration failed. Flyway error: {}. Root cause: {}",
                        exception.getMessage(),
                        rootCause.getMessage(),
                        exception);

                // Keep startup/deployment failure semantics: a failed migration must
                // not be hidden behind a successful application startup.
                throw exception;
            }
        };
    }

    private Throwable findRootCause(Throwable exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}
