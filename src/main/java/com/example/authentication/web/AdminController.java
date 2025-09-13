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

/**
 * 관리자 전용 REST API 컨트롤러
 * 
 * 이 클래스는 관리자만 접근할 수 있는 관리 기능을 제공하는 REST API 엔드포인트들을 정의합니다.
 * Spring Security의 @PreAuthorize 어노테이션을 사용하여 권한을 제어합니다.
 * 
 * 주요 기능:
 * - 사용자 활동 로그 조회 (/api/admin/users/{userId}/activities)
 * 
 * @RestController: 이 클래스를 REST 컨트롤러로 등록하고, 메서드 반환값을 JSON으로 자동 변환합니다.
 * @RequestMapping: 이 컨트롤러의 모든 엔드포인트는 "/api/admin" 경로로 시작합니다.
 * @RequiredArgsConstructor: final 필드들에 대한 생성자를 자동 생성합니다.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
	
	/**
	 * 사용자 활동 로그를 담당하는 서비스
	 * 
	 * 사용자의 활동 로그를 조회하는 데 사용됩니다.
	 */
	private final ActivityLogService activityLogService;
	
	/**
	 * 사용자 데이터 접근을 위한 Repository
	 * 
	 * 사용자 정보를 조회하는 데 사용됩니다.
	 */
	private final UserRepository userRepository;

	/**
	 * 특정 사용자의 활동 로그 조회 API (관리자 전용)
	 * 
	 * 관리자가 특정 사용자의 활동 내역을 조회할 수 있습니다.
	 * 페이징을 지원하여 대량의 로그 데이터를 효율적으로 처리합니다.
	 * 
	 * @GetMapping: HTTP GET 요청을 처리합니다.
	 * @PreAuthorize: 이 메서드는 ADMIN 역할을 가진 사용자만 접근할 수 있습니다.
	 * @PathVariable: URL 경로에서 사용자 ID를 추출합니다.
	 * @RequestParam: URL 쿼리 파라미터에서 페이징 정보를 추출합니다.
	 * 
	 * @param userId 조회할 사용자의 ID
	 * @param page 페이지 번호 (0부터 시작, 기본값: 0)
	 * @param size 페이지당 항목 수 (기본값: 20)
	 * @return ResponseEntity<Page<Map<String, Object>>> - 페이징된 사용자 활동 로그 목록
	 * 
	 * HTTP 상태 코드:
	 * - 200 OK: 활동 로그 조회 성공
	 * - 401 Unauthorized: 인증되지 않은 사용자
	 * - 403 Forbidden: ADMIN 역할이 아닌 사용자
	 * - 404 Not Found: 사용자가 존재하지 않음
	 * 
	 * 보안 특징:
	 * - ADMIN 역할을 가진 사용자만 접근 가능
	 * - JWT 토큰을 통한 인증 필요
	 * - 민감한 정보는 제외하고 활동 로그만 반환
	 * 
	 * 사용 예시:
	 * - 관리자가 특정 사용자의 의심스러운 활동 조사
	 * - 사용자 지원 시 활동 내역 확인
	 * - 보안 감사 및 모니터링
	 */
	@GetMapping("/users/{userId}/activities")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<Map<String, Object>>> getUserActivities(@PathVariable Long userId,
	                                                                  @RequestParam(defaultValue = "0") int page,
	                                                                  @RequestParam(defaultValue = "20") int size) {
		// 1. 사용자 존재 여부 확인
		User user = userRepository.findById(userId).orElseThrow();
		
		// 2. 사용자의 활동 로그를 페이징하여 조회
		Page<Map<String, Object>> logs = activityLogService.findRecent(user, page, size)
				.map(l -> Map.of(
						"id", l.getId(),                    // 로그 고유 ID
						"type", l.getType(),                // 활동 유형 (LOGIN, LOGOUT 등)
						"occurredAt", l.getOccurredAt(),    // 활동 발생 시간
						"message", l.getMessage()           // 활동에 대한 추가 설명
				));
		
		// 3. 페이징된 활동 로그를 응답으로 반환
		return ResponseEntity.ok(logs);
	}
}
