package com.example.hackathonseal;

import com.example.hackathonseal.config.DotEnvInitializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HackathonSealApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(HackathonSealApplication.class);
        app.addInitializers(new DotEnvInitializer());
        app.run(args);
    }

    @Bean
    public CommandLineRunner run(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE event_registrations ALTER COLUMN user_id DROP NOT NULL;");
                System.out.println("Successfully altered event_registrations table to drop NOT NULL constraint on user_id!");
            } catch (Exception e) {
                System.err.println("Failed to alter table: " + e.getMessage());
            }
        };
    }
}
