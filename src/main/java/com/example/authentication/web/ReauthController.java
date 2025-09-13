package com.example.authentication.web;

import com.example.authentication.domain.User;
import com.example.authentication.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reauth")
@RequiredArgsConstructor
public class ReauthController {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@PostMapping("/password")
	public ResponseEntity<Void> reauthByPassword(@RequestBody Map<String, String> body, Authentication authentication) {
		String password = body.get("password");
		if (password == null || password.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		Long userId = Long.valueOf((String) authentication.getPrincipal());
		User user = userRepository.findById(userId).orElseThrow();
		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.noContent().build();
	}
}


