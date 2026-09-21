package io.github.geodwz.sandbox.security.identity;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrentUserController {
	@GetMapping("/api/me")
	@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
	public CurrentUser me(Authentication authentication) {
		var roles = authentication.getAuthorities().stream().map(authority -> authority.getAuthority())
				.filter(authority -> authority.startsWith("ROLE_"))
				.map(authority -> authority.substring("ROLE_".length())).toList();
		return new CurrentUser(authentication.getName(), roles);
	}
	public record CurrentUser(String username, List<String> roles) { }
}
