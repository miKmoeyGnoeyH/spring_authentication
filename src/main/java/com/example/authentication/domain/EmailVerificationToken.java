package com.example.authentication.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 이메일 인증 토큰 엔티티 클래스
 * 
 * 이 클래스는 사용자의 이메일 인증을 위해 생성되는 임시 토큰을 저장합니다.
 * 사용자가 회원가입을 하면 이메일로 인증 링크가 발송되고,
 * 그 링크에는 이 엔티티에 저장된 토큰이 포함됩니다.
 * 
 * 보안 특징:
 * - 토큰은 랜덤하게 생성되어 추측하기 어렵습니다.
 * - 만료 시간이 있어 일정 시간 후에는 무효화됩니다.
 * - 한 번 사용되면 재사용할 수 없습니다.
 * 
 * @Index: token 컬럼에 인덱스를 생성하여 빠른 검색을 가능하게 합니다.
 * unique = true: 토큰은 유일해야 합니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "email_verification_tokens", indexes = {
        @Index(name = "idx_evt_token", columnList = "token", unique = true)
})
public class EmailVerificationToken {

    /**
     * 토큰 고유 식별자 (Primary Key)
     * 
     * @Id: 이 필드가 기본키(Primary Key)임을 나타냅니다.
     * @GeneratedValue: 기본키 값을 자동으로 생성합니다.
     * strategy = GenerationType.IDENTITY: 데이터베이스의 AUTO_INCREMENT 기능을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 토큰이 발급된 사용자
     * 
     * @ManyToOne: 다대일 관계를 나타냅니다. 한 사용자는 여러 인증 토큰을 가질 수 있습니다.
     * (예: 이전 토큰이 만료되어 새 토큰을 발급받는 경우)
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
     * 인증 토큰 문자열
     * 
     * 이메일로 발송되는 인증 링크에 포함되는 토큰입니다.
     * 예: "abc123def456ghi789..."
     * 
     * @Column 속성:
     * - nullable = false: NOT NULL 제약조건 (반드시 값이 있어야 함)
     * - unique = true: UNIQUE 제약조건 (중복된 토큰 불가)
     * - length = 128: 최대 길이 128자 (충분히 긴 랜덤 문자열)
     */
    @Column(nullable = false, unique = true, length = 128)
    private String token;

    /**
     * 토큰 만료 일시
     * 
     * 이 시간이 지나면 토큰이 무효화됩니다.
     * 일반적으로 24시간 또는 30분 정도로 설정합니다.
     * 
     * @Column(nullable = false): NOT NULL 제약조건 (반드시 값이 있어야 함)
     */
    @Column(nullable = false)
    private Instant expiresAt;

    /**
     * 토큰 사용 여부
     * 
     * true: 이미 사용된 토큰 (재사용 불가)
     * false: 아직 사용되지 않은 토큰
     * 
     * 보안을 위해 한 번 사용된 토큰은 재사용할 수 없도록 합니다.
     * 
     * @Column(nullable = false): NOT NULL 제약조건 (반드시 값이 있어야 함)
     */
    @Column(nullable = false)
    private boolean used;

    /**
     * 토큰을 사용됨으로 표시하는 메서드
     * 
     * 사용자가 이메일 인증 링크를 클릭했을 때 호출되어
     * used 필드를 true로 변경합니다.
     * 
     * 비즈니스 로직 메서드: 단순한 setter가 아닌, 
     * 도메인의 의미를 담은 메서드입니다.
     */
    public void markUsed() {
        this.used = true;
    }
}


