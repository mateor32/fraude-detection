package com.fraude;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifies the Spring application context starts.
 * Uses the "test" profile so it boots against an in-memory H2 database
 * (see src/test/resources/application.properties) instead of the real
 * PostgreSQL instance, which keeps CI hermetic.
 */
@SpringBootTest
@ActiveProfiles("test")
class FraudeDetectionApplicationTests {

	@Test
	void contextLoads() {
	}

}
