package com.multitalent.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Standalone microservice. Owns the "users" table (auth_db). Issues JWTs
 * containing tenantId/role claims that every other service trusts and
 * decodes locally — no shared session store needed.
 */
@SpringBootApplication(scanBasePackages = {"com.multitalent.auth", "com.multitalent.common"})
@EnableJpaAuditing
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
