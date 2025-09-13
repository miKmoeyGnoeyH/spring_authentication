package com.example.authentication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
	private final SecretKey accessKey;
	private final SecretKey refreshKey;
	private final long accessTtlSeconds;
	private final long refreshTtlSeconds;

	public JwtService(
			@Value("${app.jwt.access-secret:access-secret-please-change}") String accessSecret,
			@Value("${app.jwt.refresh-secret:refresh-secret-please-change}") String refreshSecret,
			@Value("${app.jwt.access-ttl-seconds:900}") long accessTtlSeconds,
			@Value("${app.jwt.refresh-ttl-seconds:604800}") long refreshTtlSeconds
	) {
		this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
		this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
		this.accessTtlSeconds = accessTtlSeconds;
		this.refreshTtlSeconds = refreshTtlSeconds;
	}

	public String generateAccessToken(String subject, Map<String, Object> claims) {
		Instant now = Instant.now();
		return Jwts.builder()
				.id(UUID.randomUUID().toString())
				.subject(subject)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
				.claims(claims)
				.signWith(accessKey)
				.compact();
	}

	public String generateRefreshToken(String subject, Map<String, Object> claims) {
		Instant now = Instant.now();
		return Jwts.builder()
				.id(UUID.randomUUID().toString())
				.subject(subject)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
				.claims(claims)
				.signWith(refreshKey)
				.compact();
	}

	public Claims parseAccessClaims(String token) {
		return Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token).getPayload();
	}

	public Claims parseRefreshClaims(String token) {
		return Jwts.parser().verifyWith(refreshKey).build().parseSignedClaims(token).getPayload();
	}

	public String getSubjectFromAccess(String token) {
		return parseAccessClaims(token).getSubject();
	}

	public boolean isAccessTokenValid(String token) {
		try {
			parseAccessClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
