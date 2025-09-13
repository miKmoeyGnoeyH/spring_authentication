package com.example.authentication.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 권한(Permission) 엔티티 클래스
 * 
 * 이 클래스는 시스템에서 수행할 수 있는 구체적인 권한을 정의합니다.
 * RBAC(Role-Based Access Control) 시스템에서 세밀한 권한 제어를 위해 사용됩니다.
 * 
 * 권한의 예시:
 * - user:read: 사용자 정보 조회 권한
 * - user:write: 사용자 정보 수정 권한
 * - user:delete: 사용자 삭제 권한
 * - admin:all: 모든 관리자 권한
 * 
 * 권한은 역할(Role)에 할당되며, 사용자는 역할을 통해 권한을 획득합니다.
 * 예: ADMIN 역할 → user:read, user:write, user:delete, admin:all 권한
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "permissions")
public class Permission {

    /**
     * 권한 고유 식별자 (Primary Key)
     * 
     * @Id: 이 필드가 기본키(Primary Key)임을 나타냅니다.
     * @GeneratedValue: 기본키 값을 자동으로 생성합니다.
     * strategy = GenerationType.IDENTITY: 데이터베이스의 AUTO_INCREMENT 기능을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 권한 코드
     * 
     * 권한을 식별하는 고유한 코드입니다.
     * 일반적으로 "리소스:액션" 형태로 작성합니다.
     * 
     * 예시:
     * - "user:read": 사용자 정보 조회 권한
     * - "user:write": 사용자 정보 수정 권한
     * - "user:delete": 사용자 삭제 권한
     * - "admin:all": 모든 관리자 권한
     * 
     * @Column 속성:
     * - nullable = false: NOT NULL 제약조건 (반드시 값이 있어야 함)
     * - unique = true: UNIQUE 제약조건 (중복된 권한 코드 불가)
     * - length = 64: 최대 길이 64자
     */
    @Column(nullable = false, unique = true, length = 64)
    private String code; // ex) user:read, user:write
}


