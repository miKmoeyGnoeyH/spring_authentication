package com.example.authentication.web;

import com.example.authentication.domain.User;
import com.example.authentication.repository.UserRepository;
import com.example.authentication.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
	private final ActivityLogService activityLogService;
	private final UserRepository userRepository;

	@GetMapping("/users/{userId}/activities")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<Map<String, Object>>> getUserActivities(@PathVariable Long userId,
	                                                                  @RequestParam(defaultValue = "0") int page,
	                                                                  @RequestParam(defaultValue = "20") int size) {
		User user = userRepository.findById(userId).orElseThrow();
		Page<Map<String, Object>> logs = activityLogService.findRecent(user, page, size)
				.map(l -> Map.of(
						"id", l.getId(),
						"type", l.getType(),
						"occurredAt", l.getOccurredAt(),
						"message", l.getMessage()
				));
		return ResponseEntity.ok(logs);
	}
}
