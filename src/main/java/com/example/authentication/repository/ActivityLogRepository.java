package com.example.authentication.repository;

import com.example.authentication.domain.ActivityLog;
import com.example.authentication.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 활동 로그(ActivityLog) 데이터 접근을 위한 Repository 인터페이스
 * 
 * 이 인터페이스는 ActivityLog 엔티티에 대한 데이터베이스 작업을 수행합니다.
 * Spring Data JPA가 자동으로 구현체를 생성하여 빈으로 등록합니다.
 * 
 * 활동 로그 관리 기능:
 * - 로그 생성, 조회, 수정, 삭제
 * - 사용자별 로그 조회 (페이징 지원)
 * - 시간순 정렬 조회
 */
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
	
	/**
	 * 특정 사용자의 활동 로그를 시간 내림차순으로 조회하는 메서드 (페이징 지원)
	 * 
	 * Spring Data JPA가 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.
	 * "findBy" + "User" + "OrderBy" + "OccurredAt" + "Desc" 
	 * → "SELECT * FROM activity_logs WHERE user_id = ? ORDER BY occurred_at DESC" 쿼리 생성
	 * 
	 * @param user 조회할 사용자 객체
	 * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 등)
	 * @return Page<ActivityLog> - 페이징된 활동 로그 목록
	 * 
	 * Pageable 사용 이유:
	 * - 대량의 로그 데이터를 효율적으로 처리
	 * - 메모리 사용량 최적화
	 * - 사용자 경험 향상 (한 번에 적당한 양만 표시)
	 * 
	 * 사용 예시:
	 * - 관리자가 특정 사용자의 활동 내역 조회
	 * - 사용자 대시보드에서 최근 활동 표시
	 * - 보안 감사 시 활동 추적
	 */
	Page<ActivityLog> findByUserOrderByOccurredAtDesc(User user, Pageable pageable);
}
