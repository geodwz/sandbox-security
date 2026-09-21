package io.github.geodwz.sandbox.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class InfrastructureAndJwtContractTests {

	@Test
	void KC_001_realmExportUsesTheRequiredTutorialNamesAndPrincipals() throws Exception {
		var realmExport = Files.readString(Path.of("..", "docker", "keycloak", "realm-export.json"));

		assertThat(realmExport)
				.contains("\"realm\": \"tutorial\"")
				.contains("\"clientId\": \"project-api\"")
				.contains("\"clientId\": \"vue-client\"")
				.contains("\"clientId\": \"project-report-client\"")
				.contains("\"username\": \"alice\"")
				.contains("\"username\": \"bob\"")
				.contains("\"username\": \"charlie\"")
				.contains("\"name\": \"user\"")
				.contains("\"name\": \"editor\"")
				.contains("\"name\": \"admin\"")
				.contains("project:read")
				.contains("project:write")
				.contains("project:delete");
	}

	@Test
	void AUTH_003_AUTH_004_and_NFR_002_securityConfigurationIsAnExplicitDedicatedBoundary() {
		assertThatCode(() -> Class.forName("io.github.geodwz.sandbox.security.security.SecurityConfiguration"))
				.doesNotThrowAnyException();
	}
}
