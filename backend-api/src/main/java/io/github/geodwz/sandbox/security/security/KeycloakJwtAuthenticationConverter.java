package io.github.geodwz.sandbox.security.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.StringUtils;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String SCOPE_PREFIX = "SCOPE_";
    private final String keycloakClientId = "project-api";
    private static final Set<String> KNOWN_ROLES = Set.of("user", "editor", "admin");
    private static final Set<String> KNOWN_SCOPES = Set.of("project:read", "project:write", "project:delete");

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.addAll(extractRealmRoles(jwt));
        authorities.addAll(extractClientRoles(jwt));
        authorities.addAll(extractScopes(jwt));
        return new JwtAuthenticationToken(jwt, authorities);
    }

    // -------------------------------------------------------------------------
    // Keycloak claim extractors
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return Set.of();
        }
        Object roles = realmAccess.get("roles");
        if (!(roles instanceof Collection<?> roleList)) {
            return Set.of();
        }
        return roleList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(KNOWN_ROLES::contains)
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
                .collect(Collectors.toSet());
    }

        @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
        if (!StringUtils.hasText(keycloakClientId)) {
            return Set.of();
        }
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return Set.of();
        }
        Object clientEntry = resourceAccess.get(keycloakClientId);
        if (!(clientEntry instanceof Map<?, ?> clientMap)) {
            return Set.of();
        }
        Object roles = clientMap.get("roles");
        if (!(roles instanceof Collection<?> roleList)) {
            return Set.of();
        }
        return roleList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(KNOWN_ROLES::contains)
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
                .collect(Collectors.toSet());
    }

    private Collection<GrantedAuthority> extractScopes(Jwt jwt) {
        JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
        defaultConverter.setAuthorityPrefix(SCOPE_PREFIX);
        Collection<GrantedAuthority> fromDefault = defaultConverter.convert(jwt);
        if (fromDefault != null && !fromDefault.isEmpty()) {
            return fromDefault;
        }

        // Fallback for non-standard claim layouts
        Object scopeClaim = jwt.getClaim("scope");
        if (scopeClaim == null) {
            scopeClaim = jwt.getClaim("scp");
        }

        if (scopeClaim instanceof String spaceDelimited) {
            return Stream.of(spaceDelimited.split(" "))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .filter(KNOWN_SCOPES::contains)
                    .map(s -> new SimpleGrantedAuthority(SCOPE_PREFIX + s))
                    .collect(Collectors.toSet());
        }
        if (scopeClaim instanceof Collection<?> scopes) {
            return scopes.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(KNOWN_SCOPES::contains)
                    .map(s -> new SimpleGrantedAuthority(SCOPE_PREFIX + s))
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }
    
    
}
