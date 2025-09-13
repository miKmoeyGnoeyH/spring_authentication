package com.example.authentication.web;

import com.example.authentication.security.JwtService;
import com.example.authentication.security.RefreshTokenService;
import com.example.authentication.service.ActivityLogService;
import com.example.authentication.service.AuthService;
import com.example.authentication.service.EmailVerificationService;
import com.example.authentication.web.dto.AuthDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final EmailVerificationService emailVerificationService;
	private final ActivityLogService activityLogService;

	@PostMapping("/register")
	public ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/token/refresh")
	public ResponseEntity<AuthDtos.AuthResponse> refresh(@RequestParam("refreshToken") String refreshToken) {
		var claims = jwtService.parseRefreshClaims(refreshToken);
		String userId = claims.getSubject();
		String jti = claims.getId();
		if (refreshTokenService.isRevoked(jti) || !refreshTokenService.exists(userId, jti)) {
			return ResponseEntity.status(401).build();
		}
		var newClaims = new HashMap<String, Object>();
		newClaims.put("uid", Long.valueOf(userId));
		String access = jwtService.generateAccessToken(userId, newClaims);
		String refresh = jwtService.generateRefreshToken(userId, newClaims);
		refreshTokenService.revoke(jti, Duration.ofSeconds(3600));
		refreshTokenService.store(userId, jwtService.parseRefreshClaims(refresh).getId(), refresh, Duration.ofSeconds(604800));
		// 활동 로깅 (간단 버전: userId만 기록)
		activityLogService.record(null, "REFRESH", "userId=" + userId);
		return ResponseEntity.ok(AuthDtos.AuthResponse.builder()
				.userId(Long.valueOf(userId))
				.email(null)
				.accessToken(access)
				.refreshToken(refresh)
				.build());
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestParam("refreshToken") String refreshToken) {
		var claims = jwtService.parseRefreshClaims(refreshToken);
		refreshTokenService.revoke(claims.getId(), Duration.ofSeconds(604800));
		activityLogService.record(null, "LOGOUT", "userId=" + claims.getSubject());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/verify")
	public ResponseEntity<Void> verify(@RequestParam("token") String token) {
		emailVerificationService.verify(token);
		return ResponseEntity.noContent().build();
	}
}
