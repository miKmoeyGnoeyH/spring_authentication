package com.example.authentication.web;

import com.example.authentication.domain.User;
import com.example.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {
	private final UserRepository userRepository;

	@GetMapping
	public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
		Long userId = Long.valueOf((String) authentication.getPrincipal());
		User user = userRepository.findById(userId).orElseThrow();
		return ResponseEntity.ok(Map.of(
				"id", user.getId(),
				"email", user.getEmail(),
				"displayName", user.getDisplayName()
		));
	}
}
