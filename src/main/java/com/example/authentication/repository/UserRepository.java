package com.example.authentication.repository;

import com.example.authentication.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자(User) 데이터 접근을 위한 Repository 인터페이스
 * 
 * 이 인터페이스는 User 엔티티에 대한 데이터베이스 작업을 수행합니다.
 * Spring Data JPA가 자동으로 구현체를 생성하여 빈으로 등록합니다.
 * 
 * 주요 개념:
 * - Repository: 데이터 접근 계층의 인터페이스입니다.
 * - JpaRepository: Spring Data JPA에서 제공하는 기본 CRUD 작업을 포함하는 인터페이스입니다.
 * - 메서드 이름 규칙: Spring Data JPA는 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.
 * 
 * 상속받는 기능들:
 * - save(): 엔티티 저장/수정
 * - findById(): ID로 조회
 * - findAll(): 모든 엔티티 조회
 * - delete(): 엔티티 삭제
 * - count(): 엔티티 개수 조회
 * - existsById(): ID 존재 여부 확인
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자를 조회하는 메서드
     * 
     * Spring Data JPA가 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.
     * "findBy" + "Email" → "SELECT * FROM users WHERE email = ?" 쿼리 생성
     * 
     * @param email 조회할 사용자의 이메일 주소
     * @return Optional<User> - 사용자가 존재하면 User 객체, 없으면 Optional.empty()
     * 
     * Optional 사용 이유:
     * - null 체크를 강제하여 NullPointerException 방지
     * - 값이 없을 수 있음을 명시적으로 표현
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일이 존재하는지 확인하는 메서드
     * 
     * Spring Data JPA가 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.
     * "existsBy" + "Email" → "SELECT COUNT(*) > 0 FROM users WHERE email = ?" 쿼리 생성
     * 
     * @param email 확인할 이메일 주소
     * @return boolean - 이메일이 존재하면 true, 없으면 false
     * 
     * 사용 예시:
     * - 회원가입 시 이메일 중복 체크
     * - 로그인 시 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
}


