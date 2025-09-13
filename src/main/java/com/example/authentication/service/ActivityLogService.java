package com.example.authentication.service;

import com.example.authentication.domain.ActivityLog;
import com.example.authentication.domain.User;
import com.example.authentication.repository.ActivityLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
	private final ActivityLogRepository repository;

	@Transactional
	public void record(User user, String type, String message) {
		ActivityLog log = ActivityLog.builder()
				.user(user)
				.type(type)
				.occurredAt(Instant.now())
				.message(message)
				.build();
		repository.save(log);
	}

	public Page<ActivityLog> findRecent(User user, int page, int size) {
		return repository.findByUserOrderByOccurredAtDesc(user, PageRequest.of(page, size));
	}
}
