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

/**
 * 사용자 활동 로그 서비스 클래스
 * 
 * 이 클래스는 사용자의 중요한 활동들을 기록하고 조회하는 비즈니스 로직을 처리합니다.
 * 보안 감사, 사용자 행동 분석, 문제 해결 등을 위해 사용됩니다.
 * 
 * 주요 기능:
 * - 사용자 활동 로그 기록
 * - 사용자별 활동 로그 조회 (페이징 지원)
 * - 활동 유형별 분류 및 관리
 * 
 * @Service: 이 클래스를 Spring의 서비스 빈으로 등록합니다.
 * @RequiredArgsConstructor: final 필드들에 대한 생성자를 자동 생성합니다.
 */
@Service
@RequiredArgsConstructor
public class ActivityLogService {
	
	/**
	 * 활동 로그 데이터 접근을 위한 Repository
	 * 
	 * 활동 로그의 생성, 조회, 수정, 삭제 작업을 수행합니다.
	 */
	private final ActivityLogRepository repository;

	/**
	 * 사용자의 활동을 로그로 기록하는 메서드
	 * 
	 * 이 메서드는 사용자가 수행한 중요한 활동을 데이터베이스에 기록합니다.
	 * 예: 로그인, 로그아웃, 회원가입, 비밀번호 변경 등
	 * 
	 * @Transactional: 이 메서드가 트랜잭션 내에서 실행됩니다.
	 * 데이터베이스 작업이 실패하면 모든 변경사항이 롤백됩니다.
	 * 
	 * @param user 활동을 수행한 사용자 객체
	 * @param type 활동 유형 (예: "LOGIN", "LOGOUT", "REGISTER", "PASSWORD_CHANGE")
	 * @param message 활동에 대한 추가 설명 메시지 (선택적)
	 * 
	 * 사용 예시:
	 * - record(user, "LOGIN", "IP: 192.168.1.1에서 로그인")
	 * - record(user, "PASSWORD_CHANGE", "비밀번호 변경 완료")
	 * - record(user, "ROLE_CHANGE", "역할이 USER에서 MANAGER로 변경됨")
	 */
	@Transactional
	public void record(User user, String type, String message) {
		// 1. 활동 로그 엔티티 생성
		ActivityLog log = ActivityLog.builder()
				.user(user)                    // 활동을 수행한 사용자
				.type(type)                    // 활동 유형
				.occurredAt(Instant.now())     // 활동 발생 시간 (현재 시간)
				.message(message)              // 추가 설명 메시지
				.build();
		
		// 2. 활동 로그를 데이터베이스에 저장
		repository.save(log);
	}

	/**
	 * 특정 사용자의 최근 활동 로그를 조회하는 메서드 (페이징 지원)
	 * 
	 * 이 메서드는 지정된 사용자의 활동 로그를 시간 내림차순으로 조회합니다.
	 * 대량의 로그 데이터를 효율적으로 처리하기 위해 페이징을 지원합니다.
	 * 
	 * @param user 조회할 사용자 객체
	 * @param page 페이지 번호 (0부터 시작)
	 * @param size 페이지당 항목 수
	 * @return Page<ActivityLog> - 페이징된 활동 로그 목록
	 * 
	 * 사용 예시:
	 * - findRecent(user, 0, 10): 첫 번째 페이지에서 10개 항목 조회
	 * - findRecent(user, 1, 20): 두 번째 페이지에서 20개 항목 조회
	 * 
	 * PageRequest.of(page, size): 페이징 정보를 생성합니다.
	 * - page: 페이지 번호 (0부터 시작)
	 * - size: 페이지당 항목 수
	 */
	public Page<ActivityLog> findRecent(User user, int page, int size) {
		// Repository의 findByUserOrderByOccurredAtDesc 메서드를 호출하여
		// 사용자별로 시간 내림차순 정렬된 활동 로그를 페이징하여 조회
		return repository.findByUserOrderByOccurredAtDesc(user, PageRequest.of(page, size));
	}
}
