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

/**
 * 현재 인증된 사용자 정보 조회 컨트롤러
 * 
 * 이 클래스는 현재 로그인한 사용자의 정보를 조회하는 API를 제공합니다.
 * JWT 토큰을 통해 인증된 사용자만 접근할 수 있습니다.
 * 
 * 주요 기능:
 * - 현재 사용자의 기본 정보 조회 (/api/me)
 * 
 * @RestController: 이 클래스를 REST 컨트롤러로 등록하고, 메서드 반환값을 JSON으로 자동 변환합니다.
 * @RequestMapping: 이 컨트롤러의 모든 엔드포인트는 "/api/me" 경로로 시작합니다.
 * @RequiredArgsConstructor: final 필드들에 대한 생성자를 자동 생성합니다.
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {
	
	/**
	 * 사용자 데이터 접근을 위한 Repository
	 * 
	 * 사용자 정보를 조회하는 데 사용됩니다.
	 */
	private final UserRepository userRepository;

	/**
	 * 현재 인증된 사용자 정보 조회 API
	 * 
	 * JWT 토큰을 통해 인증된 사용자의 기본 정보를 반환합니다.
	 * 
	 * @GetMapping: HTTP GET 요청을 처리합니다.
	 * @param authentication Spring Security에서 제공하는 인증 정보 객체
	 *                       JWT 필터에서 설정한 사용자 정보가 포함됩니다.
	 * @return ResponseEntity<Map<String, Object>> - 사용자 정보 (ID, 이메일, 표시 이름)
	 * 
	 * HTTP 상태 코드:
	 * - 200 OK: 사용자 정보 조회 성공
	 * - 401 Unauthorized: 인증되지 않은 사용자 (JWT 토큰 없음 또는 유효하지 않음)
	 * - 404 Not Found: 사용자가 데이터베이스에 존재하지 않음 (매우 드문 경우)
	 * 
	 * 보안 특징:
	 * - JWT 토큰이 유효한 경우에만 접근 가능
	 * - 사용자는 자신의 정보만 조회할 수 있음
	 * - 민감한 정보(비밀번호 해시 등)는 제외하고 반환
	 * 
	 * 사용 예시:
	 * - 클라이언트에서 사용자 프로필 표시
	 * - 사용자 대시보드에서 사용자 정보 표시
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
		// 1. JWT 토큰에서 사용자 ID 추출
		// authentication.getPrincipal()은 JWT 필터에서 설정한 사용자 ID (문자열)
		Long userId = Long.valueOf((String) authentication.getPrincipal());
		
		// 2. 데이터베이스에서 사용자 정보 조회
		User user = userRepository.findById(userId).orElseThrow();
		
		// 3. 사용자 정보를 Map 형태로 변환하여 반환
		// Map.of()는 Java 9+에서 제공하는 불변 Map 생성 메서드
		return ResponseEntity.ok(Map.of(
				"id", user.getId(),           // 사용자 고유 ID
				"email", user.getEmail(),     // 사용자 이메일
				"displayName", user.getDisplayName()  // 사용자 표시 이름
		));
	}
}
