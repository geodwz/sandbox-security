package io.github.geodwz.sandbox.security.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.cors(cors -> { })
				.exceptionHandling(errors -> errors
						.authenticationEntryPoint((request, response, exception) -> writeError(response, 401, "unauthorized", "Authentication required"))
						.accessDeniedHandler((request, response, exception) -> writeError(response, 403, "forbidden", "Insufficient permissions")))
				.authorizeHttpRequests(requests -> requests
						.requestMatchers("/api/public/**").permitAll()
						.requestMatchers("/api/**").authenticated()
						.anyRequest().denyAll())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())))
				.build();
	}


	private static void writeError(jakarta.servlet.http.HttpServletResponse response, int status, String error, String message)
			throws java.io.IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(@Value("${security.cors.allowed-origin}") String origin) {
		var configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(origin));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}
}
