package com.artisoft.fitbuddy.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@TestConfiguration
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // JWT Configuration from .env
        "app.jwtSecret=pSwngYFd3hunOFWq2jNASE713ptCQ5/TGx/LUtrCfwc=",
        "app.jwtExpirationInMs=86400000",

        // Server Configuration
        "server.port=8080",
        "server.servlet.context-path=/",
        "server.ssl.enabled=false",

        // Database Configuration (mock values for testing)
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",

        // JPA Configuration
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=true",

        // Flyway Configuration for tests
        "spring.flyway.enabled=false",

        // Security Configuration
        "spring.security.user.name=testuser",
        "spring.security.user.password={bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW",

        // Logging Configuration for tests
        "logging.level.org.springframework.security=ERROR",
        "logging.level.org.springframework.web=ERROR",
        "logging.level.org.hibernate=ERROR"
})
public class TestSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
