package com.example.authentication.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshTokenService {
	private final StringRedisTemplate redis;

	public RefreshTokenService(StringRedisTemplate redis) {
		this.redis = redis;
	}

	public void store(String userId, String jti, String token, Duration ttl) {
		String key = keyUserRefresh(userId, jti);
		redis.opsForValue().set(key, token, ttl);
	}

	public boolean exists(String userId, String jti) {
		String key = keyUserRefresh(userId, jti);
		return Boolean.TRUE.equals(redis.hasKey(key));
	}

	public boolean isRevoked(String jti) {
		String key = keyBlacklist(jti);
		return Boolean.TRUE.equals(redis.hasKey(key));
	}

	public void revoke(String jti, Duration ttl) {
		String key = keyBlacklist(jti);
		redis.opsForValue().set(key, "1", ttl);
	}

	private String keyUserRefresh(String userId, String jti) {
		return "refresh:" + userId + ":" + jti;
	}

	private String keyBlacklist(String jti) {
		return "blacklist:" + jti;
	}
}
