package io.github.geodwz.sandbox.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@Tag("keycloak")
@EnabledIfEnvironmentVariable(named = "KEYCLOAK_INTEGRATION", matches = "true")
class RealKeycloakContractTests {

	@Test
	void KC_001_and_KC_006_runningKeycloakExposesTutorialRealmOidcMetadata() throws Exception {
		var issuer = System.getenv("KEYCLOAK_ISSUER_URI");
		assertThat(issuer).as("KEYCLOAK_ISSUER_URI must be supplied for real-Keycloak tests").isNotBlank();

		var request = HttpRequest.newBuilder(URI.create(issuer + "/.well-known/openid-configuration"))
				.GET()
				.build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body())
				.contains("\"issuer\"")
				.contains("\"token_endpoint\"")
				.contains("\"jwks_uri\"");
	}
}
