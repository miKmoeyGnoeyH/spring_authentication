package com.example.authentication.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 소셜 계정 연결 엔티티 클래스
 * 
 * 이 클래스는 사용자의 내부 계정과 외부 소셜 계정(Google, Facebook 등)을 연결하는 정보를 저장합니다.
 * OAuth2 로그인을 통해 사용자가 소셜 계정으로 로그인할 때 사용됩니다.
 * 
 * 주요 기능:
 * - 소셜 계정과 내부 계정의 연결 관리
 * - 같은 소셜 계정이 여러 내부 계정에 연결되는 것을 방지
 * - 소셜 계정 정보(이메일 등) 저장
 * 
 * @UniqueConstraint: provider와 providerUserId의 조합이 유일해야 함을 보장합니다.
 * 즉, 같은 소셜 계정은 하나의 내부 계정에만 연결될 수 있습니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "social_accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "providerUserId"}))
public class SocialAccount {

    /**
     * 소셜 계정 연결 고유 식별자 (Primary Key)
     * 
     * @Id: 이 필드가 기본키(Primary Key)임을 나타냅니다.
     * @GeneratedValue: 기본키 값을 자동으로 생성합니다.
     * strategy = GenerationType.IDENTITY: 데이터베이스의 AUTO_INCREMENT 기능을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 내부 사용자 계정
     * 
     * @ManyToOne: 다대일 관계를 나타냅니다. 한 사용자는 여러 소셜 계정을 연결할 수 있습니다.
     * (예: Google 계정과 Facebook 계정을 모두 연결)
     * 
     * @JoinColumn: 외래키 컬럼을 지정합니다.
     *   - name = "user_id": 데이터베이스의 외래키 컬럼 이름
     *   - nullable = false: NOT NULL 제약조건 (반드시 사용자가 있어야 함)
     * 
     * FetchType.LAZY: 지연 로딩 - 실제로 user를 사용할 때만 데이터베이스에서 조회합니다.
     * optional = false: 이 관계는 필수입니다 (null이 될 수 없음).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 소셜 로그인 제공자
     * 
     * 어떤 소셜 서비스를 통해 로그인했는지를 나타냅니다.
     * 
     * 예시:
     * - "google": Google OAuth2
     * - "facebook": Facebook OAuth2
     * - "github": GitHub OAuth2
     * 
     * @Column 속성:
     * - nullable = false: NOT NULL 제약조건 (반드시 값이 있어야 함)
     * - length = 32: 최대 길이 32자
     */
    @Column(nullable = false, length = 32)
    private String provider; // e.g., google

    /**
     * 소셜 제공자에서의 사용자 ID
     * 
     * 소셜 서비스에서 제공하는 고유한 사용자 식별자입니다.
     * 예: Google의 경우 "1234567890abcdef" 같은 형태의 ID
     * 
     * @Column 속성:
     * - nullable = false: NOT NULL 제약조건 (반드시 값이 있어야 함)
     * - length = 128: 최대 길이 128자 (소셜 서비스의 ID는 보통 길지 않음)
     */
    @Column(nullable = false, length = 128)
    private String providerUserId;

    /**
     * 소셜 계정의 이메일 주소
     * 
     * 소셜 서비스에서 제공하는 이메일 주소입니다.
     * 내부 계정의 이메일과 다를 수 있습니다.
     * 
     * @Column(length = 255): 최대 길이 255자 (선택적 필드)
     */
    @Column(length = 255)
    private String email;

    /**
     * 계정 연결 일시
     * 
     * 언제 이 소셜 계정이 내부 계정과 연결되었는지를 기록합니다.
     * 
     * @Column(nullable = false): NOT NULL 제약조건 (반드시 값이 있어야 함)
     */
    @Column(nullable = false)
    private Instant linkedAt;
}


