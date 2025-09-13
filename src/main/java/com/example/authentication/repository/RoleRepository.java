package com.example.authentication.repository;

import com.example.authentication.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 역할(Role) 데이터 접근을 위한 Repository 인터페이스
 * 
 * 이 인터페이스는 Role 엔티티에 대한 데이터베이스 작업을 수행합니다.
 * Spring Data JPA가 자동으로 구현체를 생성하여 빈으로 등록합니다.
 * 
 * 역할 관리 기능:
 * - 역할 생성, 조회, 수정, 삭제
 * - 역할 이름으로 조회
 * - 역할 존재 여부 확인
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 역할 이름으로 역할을 조회하는 메서드
     * 
     * Spring Data JPA가 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.
     * "findBy" + "Name" → "SELECT * FROM roles WHERE name = ?" 쿼리 생성
     * 
     * @param name 조회할 역할의 이름 (예: "USER", "MANAGER", "ADMIN")
     * @return Optional<Role> - 역할이 존재하면 Role 객체, 없으면 Optional.empty()
     * 
     * 사용 예시:
     * - 사용자 가입 시 기본 역할 할당
     * - 권한 체크 시 역할 정보 조회
     */
    Optional<Role> findByName(String name);
    
    /**
     * 역할 이름이 존재하는지 확인하는 메서드
     * 
     * Spring Data JPA가 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.
     * "existsBy" + "Name" → "SELECT COUNT(*) > 0 FROM roles WHERE name = ?" 쿼리 생성
     * 
     * @param name 확인할 역할의 이름
     * @return boolean - 역할이 존재하면 true, 없으면 false
     * 
     * 사용 예시:
     * - 역할 생성 시 중복 체크
     * - 시스템 초기화 시 기본 역할 존재 여부 확인
     */
    boolean existsByName(String name);
}


