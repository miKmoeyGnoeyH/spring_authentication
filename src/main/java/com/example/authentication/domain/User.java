package com.example.authentication.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * 사용자(User) 엔티티 클래스
 * 
 * 이 클래스는 데이터베이스의 'users' 테이블과 매핑되는 JPA 엔티티입니다.
 * 사용자의 기본 정보(이메일, 비밀번호, 이름 등)와 역할(Role) 정보를 저장합니다.
 * 
 * 주요 개념 설명:
 * - @Entity: 이 클래스가 JPA 엔티티임을 나타냅니다. 데이터베이스 테이블과 매핑됩니다.
 * - @Table: 매핑될 테이블 이름을 지정합니다. (name = "users")
 * - @EntityListeners: 엔티티의 생명주기 이벤트를 감지하는 리스너를 등록합니다.
 *   AuditingEntityListener는 생성일시, 수정일시를 자동으로 관리해줍니다.
 * 
 * Lombok 어노테이션 설명:
 * - @Getter: 모든 필드에 대한 getter 메서드를 자동 생성
 * - @Builder: 빌더 패턴을 자동 생성 (객체 생성 시 체이닝 방식 사용 가능)
 * - @NoArgsConstructor: 매개변수 없는 생성자 생성
 * - @AllArgsConstructor: 모든 필드를 매개변수로 받는 생성자 생성
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    /**
     * 사용자 고유 식별자 (Primary Key)
     * 
     * @Id: 이 필드가 기본키(Primary Key)임을 나타냅니다.
     * @GeneratedValue: 기본키 값을 자동으로 생성합니다.
     * strategy = GenerationType.IDENTITY: 데이터베이스의 AUTO_INCREMENT 기능을 사용합니다.
     * (MySQL의 경우 AUTO_INCREMENT, PostgreSQL의 경우 SERIAL)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 이메일 주소
     * 
     * @Email: 이메일 형식 검증 (예: user@example.com)
     * @NotBlank: null, 빈 문자열, 공백만 있는 문자열을 허용하지 않음
     * @Column: 데이터베이스 컬럼 속성 정의
     *   - nullable = false: NOT NULL 제약조건
     *   - unique = true: UNIQUE 제약조건 (중복 불가)
     *   - length = 255: 최대 길이 255자
     */
    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * 암호화된 비밀번호
     * 
     * 실제 비밀번호는 저장하지 않고, 해시(Hash)된 값을 저장합니다.
     * bcrypt 같은 암호화 알고리즘을 사용하여 원본 비밀번호를 복원할 수 없도록 합니다.
     */
    @NotBlank
    @Column(nullable = false, length = 255)
    private String passwordHash;

    /**
     * 이메일 인증 완료 여부
     * 
     * 사용자가 가입 시 이메일 인증을 완료했는지 여부를 나타냅니다.
     * false인 경우 로그인이 제한될 수 있습니다.
     */
    @Column(nullable = false)
    private boolean emailVerified;

    /**
     * 사용자 표시 이름
     * 
     * 실제 이름이나 닉네임 등, 사용자를 식별할 수 있는 표시용 이름입니다.
     * 이메일과 달리 중복이 허용됩니다.
     */
    @Column(length = 128)
    private String displayName;

    /**
     * 사용자가 가진 역할(Role)들의 집합
     * 
     * @ManyToMany: 다대다 관계를 나타냅니다. 한 사용자는 여러 역할을 가질 수 있고,
     * 한 역할은 여러 사용자에게 할당될 수 있습니다.
     * 
     * @JoinTable: 다대다 관계를 위한 중간 테이블을 정의합니다.
     *   - name = "user_roles": 중간 테이블 이름
     *   - joinColumns: 현재 엔티티(User)의 외래키 컬럼
     *   - inverseJoinColumns: 반대편 엔티티(Role)의 외래키 컬럼
     * 
     * @Builder.Default: Builder 패턴 사용 시 기본값을 설정합니다.
     * 
     * FetchType.LAZY: 지연 로딩 - 실제로 roles를 사용할 때만 데이터베이스에서 조회합니다.
     * (성능 최적화를 위한 설정)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * 레코드 생성 일시
     * 
     * @CreatedDate: 엔티티가 처음 저장될 때 자동으로 현재 시간이 설정됩니다.
     * @Column(updatable = false): 한 번 설정되면 수정할 수 없습니다.
     * 
     * AuditingEntityListener가 이 어노테이션을 감지하여 자동으로 값을 설정합니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 레코드 마지막 수정 일시
     * 
     * @LastModifiedDate: 엔티티가 수정될 때마다 자동으로 현재 시간으로 업데이트됩니다.
     * 
     * AuditingEntityListener가 이 어노테이션을 감지하여 자동으로 값을 설정합니다.
     */
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * 이메일 인증을 완료로 표시하는 메서드
     * 
     * 사용자가 이메일 인증 링크를 클릭했을 때 호출되어
     * emailVerified 필드를 true로 변경합니다.
     * 
     * 비즈니스 로직 메서드: 단순한 getter/setter가 아닌, 
     * 도메인의 의미를 담은 메서드입니다.
     */
    public void markEmailVerified() {
        this.emailVerified = true;
    }
}


