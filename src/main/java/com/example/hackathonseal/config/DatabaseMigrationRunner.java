package com.example.hackathonseal.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationRunner.class);
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Running custom database migration to drop deprecated 'score' column from evaluations table...");
            jdbcTemplate.execute("ALTER TABLE evaluations DROP COLUMN IF EXISTS score;");
            log.info("Database migration completed successfully.");
        } catch (Exception e) {
            log.error("Failed to run database migration: {}", e.getMessage());
        }
    }
}
