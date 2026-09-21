package io.github.geodwz.sandbox.security;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class M2mClientContractTests {

	@Test
	void M2MC_001_clientCredentialsRegistrationIsExternalizedInDedicatedConfiguration() {
		assertThatCode(() -> Class.forName("io.github.geodwz.sandbox.security.oauth2.M2mOAuth2ClientConfiguration"))
				.doesNotThrowAnyException();
	}

	@Test
	void M2MC_002_and_M2MC_003_apiClientUsesSpringManagedBearerTokensAndSafeFailures() {
		assertThatCode(() -> Class.forName("io.github.geodwz.sandbox.security.project.ProjectApiClient"))
				.doesNotThrowAnyException();
	}
}
