package com.example.authentication.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 역할-권한 연결 테이블 엔티티 클래스
 * 
 * 이 클래스는 역할(Role)과 권한(Permission) 간의 다대다 관계를 나타내는 중간 테이블입니다.
 * 한 역할은 여러 권한을 가질 수 있고, 한 권한은 여러 역할에 할당될 수 있습니다.
 * 
 * 예시:
 * - ADMIN 역할 → user:read, user:write, user:delete, admin:all 권한
 * - MANAGER 역할 → user:read, user:write 권한
 * - USER 역할 → user:read 권한
 * 
 * @UniqueConstraint: role_id와 permission_id의 조합이 유일해야 함을 보장합니다.
 * 즉, 같은 역할에 같은 권한을 중복으로 할당할 수 없습니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "role_permissions", uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id"}))
public class RolePermission {

    /**
     * 연결 테이블 고유 식별자 (Primary Key)
     * 
     * @Id: 이 필드가 기본키(Primary Key)임을 나타냅니다.
     * @GeneratedValue: 기본키 값을 자동으로 생성합니다.
     * strategy = GenerationType.IDENTITY: 데이터베이스의 AUTO_INCREMENT 기능을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 역할(Role)
     * 
     * @ManyToOne: 다대일 관계를 나타냅니다. 여러 RolePermission이 하나의 Role을 참조할 수 있습니다.
     * 
     * @JoinColumn: 외래키 컬럼을 지정합니다.
     *   - name = "role_id": 데이터베이스의 외래키 컬럼 이름
     *   - nullable = false: NOT NULL 제약조건 (반드시 역할이 있어야 함)
     * 
     * FetchType.LAZY: 지연 로딩 - 실제로 role을 사용할 때만 데이터베이스에서 조회합니다.
     * optional = false: 이 관계는 필수입니다 (null이 될 수 없음).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * 연결된 권한(Permission)
     * 
     * @ManyToOne: 다대일 관계를 나타냅니다. 여러 RolePermission이 하나의 Permission을 참조할 수 있습니다.
     * 
     * @JoinColumn: 외래키 컬럼을 지정합니다.
     *   - name = "permission_id": 데이터베이스의 외래키 컬럼 이름
     *   - nullable = false: NOT NULL 제약조건 (반드시 권한이 있어야 함)
     * 
     * FetchType.LAZY: 지연 로딩 - 실제로 permission을 사용할 때만 데이터베이스에서 조회합니다.
     * optional = false: 이 관계는 필수입니다 (null이 될 수 없음).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
}


