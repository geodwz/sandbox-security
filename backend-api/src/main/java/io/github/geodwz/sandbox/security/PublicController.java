package io.github.geodwz.sandbox.security;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PublicController {
	@GetMapping("/api/public/health") Map<String, String> health() { return Map.of("status", "UP"); }
}
