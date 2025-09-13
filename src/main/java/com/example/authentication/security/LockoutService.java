package com.example.authentication.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LockoutService {
	private final StringRedisTemplate redis;
	private final int maxFailures;
	private final Duration failureWindow;
	private final Duration lockoutDuration;

	public LockoutService(StringRedisTemplate redis,
	                     @Value("${app.lockout.max-failures:5}") int maxFailures,
	                     @Value("${app.lockout.failure-window-seconds:900}") long failureWindowSeconds,
	                     @Value("${app.lockout.lockout-seconds:900}") long lockoutSeconds) {
		this.redis = redis;
		this.maxFailures = maxFailures;
		this.failureWindow = Duration.ofSeconds(failureWindowSeconds);
		this.lockoutDuration = Duration.ofSeconds(lockoutSeconds);
	}

	public boolean isLocked(String principalKey) {
		String key = keyLock(principalKey);
		return Boolean.TRUE.equals(redis.hasKey(key));
	}

	public void recordFailure(String principalKey) {
		String failKey = keyFail(principalKey);
		Long count = redis.opsForValue().increment(failKey);
		if (count != null && count == 1) {
			redis.expire(failKey, failureWindow);
		}
		if (count != null && count >= maxFailures) {
			lock(principalKey);
		}
	}

	public void resetFailures(String principalKey) {
		redis.delete(keyFail(principalKey));
	}

	public void lock(String principalKey) {
		redis.opsForValue().set(keyLock(principalKey), "1", lockoutDuration);
	}

	private String keyFail(String principalKey) {
		return "login:fail:" + principalKey;
	}

	private String keyLock(String principalKey) {
		return "login:lock:" + principalKey;
	}
}
