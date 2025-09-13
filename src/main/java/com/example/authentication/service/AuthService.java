package com.example.authentication.service;

import com.example.authentication.domain.Role;
import com.example.authentication.domain.User;
import com.example.authentication.repository.RoleRepository;
import com.example.authentication.repository.UserRepository;
import com.example.authentication.security.JwtService;
import com.example.authentication.security.LockoutService;
import com.example.authentication.security.RefreshTokenService;
import com.example.authentication.web.dto.AuthDtos;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final LockoutService lockoutService;
	private final EmailVerificationService emailVerificationService;
	private final ActivityLogService activityLogService;

	@Transactional
	public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}
		User user = User.builder()
				.email(request.getEmail())
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.displayName(request.getDisplayName())
				.emailVerified(false)
				.build();
		roleRepository.findByName("USER").ifPresent(r -> user.getRoles().add(r));
		User saved = userRepository.save(user);

		// 인증 메일 발송
		emailVerificationService.sendVerificationEmail(saved);
		activityLogService.record(saved, "REGISTER", "사용자 가입");

		Map<String, Object> claims = new HashMap<>();
		claims.put("uid", saved.getId());
		String access = jwtService.generateAccessToken(String.valueOf(saved.getId()), claims);
		String refresh = jwtService.generateRefreshToken(String.valueOf(saved.getId()), claims);
		refreshTokenService.store(String.valueOf(saved.getId()), jwtService.parseRefreshClaims(refresh).getId(), refresh, Duration.ofSeconds(604800));

		return AuthDtos.AuthResponse.builder()
				.userId(saved.getId())
				.email(saved.getEmail())
				.accessToken(access)
				.refreshToken(refresh)
				.build();
	}

	@Transactional
	public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
		String principal = request.getEmail().toLowerCase();
		if (lockoutService.isLocked(principal)) {
			throw new IllegalStateException("로그인 시도가 일시적으로 차단되었습니다. 잠시 후 다시 시도하세요.");
		}
		User user = userRepository.findByEmail(request.getEmail())
				.orElse(null);
		if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			lockoutService.recordFailure(principal);
			throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
		}
		if (!user.isEmailVerified()) {
			throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
		}
		lockoutService.resetFailures(principal);
		Map<String, Object> claims = new HashMap<>();
		claims.put("uid", user.getId());
		String access = jwtService.generateAccessToken(String.valueOf(user.getId()), claims);
		String refresh = jwtService.generateRefreshToken(String.valueOf(user.getId()), claims);
		refreshTokenService.store(String.valueOf(user.getId()), jwtService.parseRefreshClaims(refresh).getId(), refresh, Duration.ofSeconds(604800));
		activityLogService.record(user, "LOGIN", "로그인 성공");
		return AuthDtos.AuthResponse.builder()
				.userId(user.getId())
				.email(user.getEmail())
				.accessToken(access)
				.refreshToken(refresh)
				.build();
	}
}
