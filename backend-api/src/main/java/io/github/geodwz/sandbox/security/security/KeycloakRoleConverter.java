package io.github.geodwz.sandbox.security.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public final class KeycloakRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private static final Set<String> KNOWN_ROLES = Set.of("user", "editor", "admin");
	private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		var authorities = new LinkedHashSet<>(scopeConverter.convert(jwt));
		authorities.addAll(roleAuthorities(jwt));
		return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username"));
	}

	@SuppressWarnings("unchecked")
	private Collection<org.springframework.security.core.GrantedAuthority> roleAuthorities(Jwt jwt) {
		var resourceAccess = jwt.getClaimAsMap("resource_access");
		if (resourceAccess == null || !(resourceAccess.get("project-api") instanceof Map<?, ?> clientAccess)) {
			return List.of();
		}
		var roles = clientAccess.get("roles");
		if (!(roles instanceof Collection<?> values)) {
			return List.of();
		}
		return values.stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.filter(KNOWN_ROLES::contains)
				.map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
				.map(org.springframework.security.core.GrantedAuthority.class::cast)
				.toList();
	}
}
