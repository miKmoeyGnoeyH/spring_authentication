package com.example.authentication.repository;

import com.example.authentication.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 권한(Permission) 데이터 접근을 위한 Repository 인터페이스
 * 
 * 이 인터페이스는 Permission 엔티티에 대한 데이터베이스 작업을 수행합니다.
 * Spring Data JPA가 자동으로 구현체를 생성하여 빈으로 등록합니다.
 * 
 * 권한 관리 기능:
 * - 권한 생성, 조회, 수정, 삭제
 * - 권한 코드로 조회
 * - 권한 존재 여부 확인
 */
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * 권한 코드로 권한을 조회하는 메서드
     * 
     * Spring Data JPA가 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.
     * "findBy" + "Code" → "SELECT * FROM permissions WHERE code = ?" 쿼리 생성
     * 
     * @param code 조회할 권한의 코드 (예: "user:read", "user:write", "admin:all")
     * @return Optional<Permission> - 권한이 존재하면 Permission 객체, 없으면 Optional.empty()
     * 
     * 사용 예시:
     * - 역할에 권한 할당 시 권한 정보 조회
     * - 권한 체크 시 권한 정보 확인
     */
    Optional<Permission> findByCode(String code);
    
    /**
     * 권한 코드가 존재하는지 확인하는 메서드
     * 
     * Spring Data JPA가 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.
     * "existsBy" + "Code" → "SELECT COUNT(*) > 0 FROM permissions WHERE code = ?" 쿼리 생성
     * 
     * @param code 확인할 권한의 코드
     * @return boolean - 권한이 존재하면 true, 없으면 false
     * 
     * 사용 예시:
     * - 권한 생성 시 중복 체크
     * - 시스템 초기화 시 기본 권한 존재 여부 확인
     */
    boolean existsByCode(String code);
}


