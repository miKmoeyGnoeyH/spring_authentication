package com.example.authentication.repository;

import com.example.authentication.domain.ActivityLog;
import com.example.authentication.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
	Page<ActivityLog> findByUserOrderByOccurredAtDesc(User user, Pageable pageable);
}
