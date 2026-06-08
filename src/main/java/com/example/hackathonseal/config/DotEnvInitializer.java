package com.example.hackathonseal.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads variables from the .env file at project root and adds them
 * as a Spring PropertySource BEFORE application.properties is resolved.
 *
 * This means ${DB_PASSWORD} in application.properties will be replaced
 * with the value from .env automatically.
 */
public class DotEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()   // Don't crash if .env doesn't exist (e.g. in CI/CD)
                .load();

        Map<String, Object> envVars = new HashMap<>();
        for (DotenvEntry entry : dotenv.entries()) {
            envVars.put(entry.getKey(), entry.getValue());
        }

        // Register as highest-priority property source so .env overrides defaults
        applicationContext.getEnvironment()
                .getPropertySources()
                .addFirst(new MapPropertySource("dotenvProperties", envVars));
    }
}
