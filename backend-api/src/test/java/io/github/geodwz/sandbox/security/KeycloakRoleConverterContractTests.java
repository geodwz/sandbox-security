package io.github.geodwz.sandbox.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakRoleConverterContractTests {

	@Test
	void ROLE_001_to_ROLE_005_converterMapsKnownRolesAndFailsClosed() {
		assertThat(authoritiesFor(List.of("user"))).containsExactly("ROLE_USER");
		assertThat(authoritiesFor(List.of("editor", "user"))).containsExactlyInAnyOrder("ROLE_EDITOR", "ROLE_USER");
		assertThat(authoritiesFor(List.of("admin", "editor", "user")))
				.containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_EDITOR", "ROLE_USER");
		assertThat(authoritiesFor(List.of("unknown"))).isEmpty();
		assertThat(authoritiesForWithoutResourceAccess()).isEmpty();
	}

	@Test
	void ROLE_004_converterClassExistsAsTheDedicatedClaimBoundary() {
		assertThatCode(() -> Class.forName("io.github.geodwz.sandbox.security.security.KeycloakRoleConverter"))
				.doesNotThrowAnyException();
	}

	@SuppressWarnings("unchecked")
	private Set<String> authoritiesFor(List<String> roles) {
		var resourceAccess = Map.of("project-api", Map.of("roles", roles));
		return authoritiesFor(jwtWithClaim("resource_access", resourceAccess));
	}

	private Set<String> authoritiesForWithoutResourceAccess() {
		return authoritiesFor(Jwt.withTokenValue("test-token")
				.header("alg", "none")
				.claim("sub", "alice")
				.build());
	}

	@SuppressWarnings("unchecked")
	private Set<String> authoritiesFor(Jwt jwt) {
		try {
			var converterClass = Class.forName("io.github.geodwz.sandbox.security.security.KeycloakRoleConverter");
			var converter = (Converter<Jwt, AbstractAuthenticationToken>) converterClass.getConstructor().newInstance();
			return converter.convert(jwt).getAuthorities().stream()
					.map(authority -> authority.getAuthority())
					.collect(Collectors.toSet());
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Expected dedicated KeycloakRoleConverter is not implemented", exception);
		}
	}

	private Jwt jwtWithClaim(String name, Object value) {
		return Jwt.withTokenValue("test-token")
				.header("alg", "none")
				.claim("sub", "alice")
				.claim(name, value)
				.build();
	}
}
