package com.example.authentication.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 역할(Role) 엔티티 클래스
 * 
 * 이 클래스는 사용자의 역할을 정의하는 엔티티입니다.
 * RBAC(Role-Based Access Control) 시스템에서 사용자의 권한을 관리하기 위해 사용됩니다.
 * 
 * 역할의 종류:
 * - USER: 일반 사용자 (기본 권한)
 * - MANAGER: 관리자 (사용자 관리 권한)
 * - ADMIN: 최고 관리자 (모든 권한)
 * 
 * Spring Security에서 이 역할들은 "ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN" 형태로 사용됩니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "roles")
public class Role {

    /**
     * 역할 고유 식별자 (Primary Key)
     * 
     * @Id: 이 필드가 기본키(Primary Key)임을 나타냅니다.
     * @GeneratedValue: 기본키 값을 자동으로 생성합니다.
     * strategy = GenerationType.IDENTITY: 데이터베이스의 AUTO_INCREMENT 기능을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 역할 이름
     * 
     * 역할의 고유한 이름을 저장합니다.
     * 예: "USER", "MANAGER", "ADMIN"
     * 
     * @Column 속성:
     * - nullable = false: NOT NULL 제약조건 (반드시 값이 있어야 함)
     * - unique = true: UNIQUE 제약조건 (중복된 역할 이름 불가)
     * - length = 32: 최대 길이 32자 (역할 이름은 보통 짧음)
     */
    @Column(nullable = false, unique = true, length = 32)
    private String name; // USER, MANAGER, ADMIN
}


