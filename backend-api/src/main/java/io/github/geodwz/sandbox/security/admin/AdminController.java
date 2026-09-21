package io.github.geodwz.sandbox.security.admin;

import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
	@GetMapping("/api/admin/users") public Map<String, Object> users() { return Map.of("users", 3); }
	@GetMapping("/api/admin/security") public Map<String, Object> security() { return Map.of("status", "secured"); }
}
