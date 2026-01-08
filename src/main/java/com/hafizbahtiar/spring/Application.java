package com.hafizbahtiar.spring;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application.
 * Loads .env file before starting Spring Boot (if present).
 */
@SpringBootApplication
@Slf4j
public class Application {

	public static void main(String[] args) {
		// Load .env file before Spring Boot starts (for local development)
		loadDotEnv();

		SpringApplication.run(Application.class, args);
	}

	/**
	 * Load environment variables from .env file.
	 * This allows local development with .env file while production uses actual
	 * environment variables.
	 */
	private static void loadDotEnv() {
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing() // Don't fail if .env file doesn't exist
					.load();

			// Load all variables from .env into system properties
			// Spring Boot will read these via ${VAR_NAME} syntax
			dotenv.entries().forEach(entry -> {
				String key = entry.getKey();
				String value = entry.getValue();

				// Only set if not already set (system env vars take precedence)
				if (System.getProperty(key) == null && System.getenv(key) == null) {
					System.setProperty(key, value);
					log.debug("Loaded from .env: {}", key);
				}
			});

			log.info("Loaded .env file successfully (if present)");
		} catch (Exception e) {
			log.debug("Could not load .env file (OK if using system environment variables): {}", e.getMessage());
		}
	}
}
