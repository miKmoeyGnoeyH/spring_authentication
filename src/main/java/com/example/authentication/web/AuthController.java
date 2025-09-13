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

/**
 * 인증 관련 REST API 컨트롤러
 * 
 * 이 클래스는 사용자 인증과 관련된 HTTP 요청을 처리하는 REST API 엔드포인트들을 제공합니다.
 * Spring MVC의 @RestController 어노테이션을 사용하여 JSON 응답을 자동으로 처리합니다.
 * 
 * 주요 기능:
 * - 사용자 회원가입 (/api/auth/register)
 * - 사용자 로그인 (/api/auth/login)
 * - JWT 토큰 갱신 (/api/auth/token/refresh)
 * - 사용자 로그아웃 (/api/auth/logout)
 * - 이메일 인증 (/api/auth/verify)
 * 
 * @RestController: 이 클래스를 REST 컨트롤러로 등록하고, 메서드 반환값을 JSON으로 자동 변환합니다.
 * @RequestMapping: 이 컨트롤러의 모든 엔드포인트는 "/api/auth" 경로로 시작합니다.
 * @RequiredArgsConstructor: final 필드들에 대한 생성자를 자동 생성합니다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	
	/**
	 * 인증 비즈니스 로직을 처리하는 서비스
	 * 
	 * 회원가입, 로그인 등의 핵심 인증 로직을 담당합니다.
	 */
	private final AuthService authService;
	
	/**
	 * JWT 토큰 생성 및 검증을 담당하는 서비스
	 * 
	 * 액세스 토큰과 갱신 토큰의 생성, 파싱, 검증을 처리합니다.
	 */
	private final JwtService jwtService;
	
	/**
	 * 갱신 토큰 관리를 담당하는 서비스
	 * 
	 * 갱신 토큰의 저장, 검증, 무효화를 처리합니다.
	 */
	private final RefreshTokenService refreshTokenService;
	
	/**
	 * 이메일 인증을 담당하는 서비스
	 * 
	 * 이메일 인증 토큰 생성, 발송, 검증을 처리합니다.
	 */
	private final EmailVerificationService emailVerificationService;
	
	/**
	 * 사용자 활동 로그를 담당하는 서비스
	 * 
	 * 로그인, 로그아웃, 토큰 갱신 등의 활동을 기록합니다.
	 */
	private final ActivityLogService activityLogService;

	/**
	 * 사용자 회원가입 API
	 * 
	 * 새로운 사용자를 등록하고 JWT 토큰을 발급합니다.
	 * 
	 * @PostMapping: HTTP POST 요청을 처리합니다.
	 * @Valid: 요청 데이터의 유효성을 검사합니다 (@Email, @NotBlank 등).
	 * @RequestBody: HTTP 요청 본문을 RegisterRequest 객체로 변환합니다.
	 * 
	 * @param request 회원가입 요청 데이터 (이메일, 비밀번호, 표시 이름)
	 * @return ResponseEntity<AuthDtos.AuthResponse> - 회원가입 성공 시 사용자 정보와 JWT 토큰
	 * 
	 * HTTP 상태 코드:
	 * - 200 OK: 회원가입 성공
	 * - 400 Bad Request: 잘못된 요청 데이터 (유효성 검사 실패)
	 * - 409 Conflict: 이미 존재하는 이메일
	 */
	@PostMapping("/register")
	public ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	/**
	 * 사용자 로그인 API
	 * 
	 * 이메일과 비밀번호로 사용자를 인증하고 JWT 토큰을 발급합니다.
	 * 
	 * @PostMapping: HTTP POST 요청을 처리합니다.
	 * @Valid: 요청 데이터의 유효성을 검사합니다.
	 * @RequestBody: HTTP 요청 본문을 LoginRequest 객체로 변환합니다.
	 * 
	 * @param request 로그인 요청 데이터 (이메일, 비밀번호)
	 * @return ResponseEntity<AuthDtos.AuthResponse> - 로그인 성공 시 사용자 정보와 JWT 토큰
	 * 
	 * HTTP 상태 코드:
	 * - 200 OK: 로그인 성공
	 * - 400 Bad Request: 잘못된 요청 데이터
	 * - 401 Unauthorized: 잘못된 이메일 또는 비밀번호
	 * - 423 Locked: 계정이 잠금 상태
	 * - 403 Forbidden: 이메일 인증이 완료되지 않음
	 */
	@PostMapping("/login")
	public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	/**
	 * JWT 토큰 갱신 API
	 * 
	 * 만료된 액세스 토큰을 새로운 토큰으로 갱신합니다.
	 * 갱신 토큰이 유효한 경우에만 새로운 액세스 토큰과 갱신 토큰을 발급합니다.
	 * 
	 * @PostMapping: HTTP POST 요청을 처리합니다.
	 * @RequestParam: URL 쿼리 파라미터에서 갱신 토큰을 추출합니다.
	 * 
	 * @param refreshToken 갱신 토큰 문자열
	 * @return ResponseEntity<AuthDtos.AuthResponse> - 토큰 갱신 성공 시 새로운 JWT 토큰들
	 * 
	 * HTTP 상태 코드:
	 * - 200 OK: 토큰 갱신 성공
	 * - 401 Unauthorized: 갱신 토큰이 유효하지 않거나 만료됨
	 * 
	 * 보안 특징:
	 * - 기존 갱신 토큰은 무효화되어 재사용할 수 없습니다.
	 * - 새로운 갱신 토큰이 발급되어 토큰 순환을 보장합니다.
	 */
	@PostMapping("/token/refresh")
	public ResponseEntity<AuthDtos.AuthResponse> refresh(@RequestParam("refreshToken") String refreshToken) {
		// 1. 갱신 토큰 파싱 및 검증
		var claims = jwtService.parseRefreshClaims(refreshToken);
		String userId = claims.getSubject();
		String jti = claims.getId();
		
		// 2. 토큰 유효성 검사
		if (refreshTokenService.isRevoked(jti) || !refreshTokenService.exists(userId, jti)) {
			return ResponseEntity.status(401).build();
		}
		
		// 3. 새로운 토큰 생성
		var newClaims = new HashMap<String, Object>();
		newClaims.put("uid", Long.valueOf(userId));
		String access = jwtService.generateAccessToken(userId, newClaims);
		String refresh = jwtService.generateRefreshToken(userId, newClaims);
		
		// 4. 기존 갱신 토큰 무효화 및 새 토큰 저장
		refreshTokenService.revoke(jti, Duration.ofSeconds(3600));
		refreshTokenService.store(userId, jwtService.parseRefreshClaims(refresh).getId(), refresh, Duration.ofSeconds(604800));
		
		// 5. 활동 로깅 (간단 버전: userId만 기록)
		activityLogService.record(null, "REFRESH", "userId=" + userId);
		
		// 6. 새로운 토큰들을 응답으로 반환
		return ResponseEntity.ok(AuthDtos.AuthResponse.builder()
				.userId(Long.valueOf(userId))
				.email(null)  // 갱신 시에는 이메일 정보를 포함하지 않음
				.accessToken(access)
				.refreshToken(refresh)
				.build());
	}

	/**
	 * 사용자 로그아웃 API
	 * 
	 * 갱신 토큰을 무효화하여 사용자를 로그아웃 처리합니다.
	 * 
	 * @PostMapping: HTTP POST 요청을 처리합니다.
	 * @RequestParam: URL 쿼리 파라미터에서 갱신 토큰을 추출합니다.
	 * 
	 * @param refreshToken 무효화할 갱신 토큰 문자열
	 * @return ResponseEntity<Void> - 로그아웃 성공 (본문 없음)
	 * 
	 * HTTP 상태 코드:
	 * - 204 No Content: 로그아웃 성공
	 * 
	 * 보안 특징:
	 * - 갱신 토큰이 서버에서 무효화되어 재사용할 수 없습니다.
	 * - 액세스 토큰은 만료될 때까지 유효하지만, 갱신할 수 없습니다.
	 */
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestParam("refreshToken") String refreshToken) {
		// 1. 갱신 토큰 파싱
		var claims = jwtService.parseRefreshClaims(refreshToken);
		
		// 2. 갱신 토큰 무효화 (7일간 유지)
		refreshTokenService.revoke(claims.getId(), Duration.ofSeconds(604800));
		
		// 3. 활동 로깅
		activityLogService.record(null, "LOGOUT", "userId=" + claims.getSubject());
		
		// 4. 성공 응답 반환 (본문 없음)
		return ResponseEntity.noContent().build();
	}

	/**
	 * 이메일 인증 API
	 * 
	 * 사용자가 이메일로 받은 인증 링크를 클릭했을 때 호출됩니다.
	 * 토큰을 검증하고 사용자의 이메일 인증을 완료 처리합니다.
	 * 
	 * @GetMapping: HTTP GET 요청을 처리합니다.
	 * @RequestParam: URL 쿼리 파라미터에서 인증 토큰을 추출합니다.
	 * 
	 * @param token 이메일 인증 토큰 문자열
	 * @return ResponseEntity<Void> - 인증 성공 (본문 없음)
	 * 
	 * HTTP 상태 코드:
	 * - 204 No Content: 이메일 인증 성공
	 * - 400 Bad Request: 잘못된 토큰
	 * - 410 Gone: 토큰이 만료되었거나 이미 사용됨
	 * 
	 * 사용 방법:
	 * - 사용자가 이메일의 인증 링크를 클릭하면 이 API가 호출됩니다.
	 * - URL 예시: /api/auth/verify?token=abc123def456
	 */
	@GetMapping("/verify")
	public ResponseEntity<Void> verify(@RequestParam("token") String token) {
		emailVerificationService.verify(token);
		return ResponseEntity.noContent().build();
	}
}
