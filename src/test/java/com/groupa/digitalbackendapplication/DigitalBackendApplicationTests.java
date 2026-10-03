package com.groupa.digitalbackendapplication;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class DigitalBackendApplicationTests {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void overrideProps(DynamicPropertyRegistry registry) {
		registry.add("db", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("JWT_SECRET", () -> "test-secret-at-least-32-characters-long");
		registry.add("MAIL_USERNAME", () -> "test@example.com");
		registry.add("MAIL_FROM", () -> "test@example.com");
		registry.add("MAIL_PASSWORD", () -> "test-password");
		registry.add("JWT_EXPIRATION_TIME", () -> "360000");
		registry.add("JWT_REFRESH_EXPIRATION_TIME", () -> "3600000");
		registry.add("ENCRYPTION_SALT", ()-> "191d497a3084231f5c2c50413b25c1ed");
		registry.add("ENCRYPTION_PASSWORD",  ()-> "test-encryption-password");
	}

	@Test
	void contextLoads() {
	}

}
