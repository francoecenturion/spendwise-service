package com.spendwise;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SpendwiseServiceApplication {

	public static void main(String[] args) {
		// Load .env file for local development; ignored if file is absent (e.g. in production).
		// Only sets a system property when the variable is not already provided by the environment,
		// so real env vars (Render, CI) always take precedence.
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(entry -> {
			if (System.getenv(entry.getKey()) == null) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		});

		SpringApplication.run(SpendwiseServiceApplication.class, args);
	}

}
