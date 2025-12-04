package com.company.point.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration to load environment variables from .env file.
 * This loads .env file values into Spring's environment before application starts.
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        // Load .env file (will look in project root by default)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()  // Don't fail if .env doesn't exist (e.g., in production)
                .load();

        // Create a map of environment variables from .env file
        Map<String, Object> envMap = new HashMap<>();
        dotenv.entries().forEach(entry -> {
            envMap.put(entry.getKey(), entry.getValue());
        });

        // Add .env variables to Spring environment with higher priority
        environment.getPropertySources().addFirst(
                new MapPropertySource("dotenvProperties", envMap)
        );
    }
}
