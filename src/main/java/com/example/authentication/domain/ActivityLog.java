package com.example.authentication.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 사용자 활동 로그 엔티티 클래스
 * 
 * 이 클래스는 사용자의 중요한 활동들을 기록하는 로그 테이블입니다.
 * 보안 감사, 사용자 행동 분석, 문제 해결 등을 위해 사용됩니다.
 * 
 * 기록되는 활동의 종류:
 * - LOGIN: 로그인
 * - LOGOUT: 로그아웃
 * - REGISTER: 회원가입
 * - REFRESH: 토큰 갱신
 * - PASSWORD_CHANGE: 비밀번호 변경
 * - ROLE_CHANGE: 역할 변경
 * 
 * @Index: user_id와 occurredAt 컬럼에 복합 인덱스를 생성하여
 * 특정 사용자의 활동을 시간순으로 빠르게 조회할 수 있게 합니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_user_time", columnList = "user_id, occurredAt")
})
public class ActivityLog {

    /**
     * 로그 고유 식별자 (Primary Key)
     * 
     * @Id: 이 필드가 기본키(Primary Key)임을 나타냅니다.
     * @GeneratedValue: 기본키 값을 자동으로 생성합니다.
     * strategy = GenerationType.IDENTITY: 데이터베이스의 AUTO_INCREMENT 기능을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 활동을 수행한 사용자
     * 
     * @ManyToOne: 다대일 관계를 나타냅니다. 한 사용자는 여러 활동 로그를 가질 수 있습니다.
     * 
     * @JoinColumn: 외래키 컬럼을 지정합니다.
     *   - name = "user_id": 데이터베이스의 외래키 컬럼 이름
     * 
     * FetchType.LAZY: 지연 로딩 - 실제로 user를 사용할 때만 데이터베이스에서 조회합니다.
     * nullable = true: 사용자가 삭제되어도 로그는 보존할 수 있습니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 활동 유형
     * 
     * 활동의 종류를 나타내는 문자열입니다.
     * 
     * 예시:
     * - "LOGIN": 로그인
     * - "LOGOUT": 로그아웃
     * - "REGISTER": 회원가입
     * - "REFRESH": 토큰 갱신
     * - "PASSWORD_CHANGE": 비밀번호 변경
     * - "ROLE_CHANGE": 역할 변경
     * 
     * @Column 속성:
     * - nullable = false: NOT NULL 제약조건 (반드시 값이 있어야 함)
     * - length = 64: 최대 길이 64자
     */
    @Column(nullable = false, length = 64)
    private String type; // LOGIN, LOGOUT, REGISTER, REFRESH, PASSWORD_CHANGE, ROLE_CHANGE

    /**
     * 활동 발생 일시
     * 
     * 활동이 언제 발생했는지를 기록합니다.
     * 로그 분석 시 시간순 정렬이나 특정 기간 조회에 사용됩니다.
     * 
     * @Column(nullable = false): NOT NULL 제약조건 (반드시 값이 있어야 함)
     */
    @Column(nullable = false)
    private Instant occurredAt;

    /**
     * 활동에 대한 추가 설명 메시지
     * 
     * 활동의 세부 내용이나 컨텍스트를 기록합니다.
     * 예: "IP: 192.168.1.1에서 로그인", "역할이 USER에서 MANAGER로 변경됨"
     * 
     * @Column(length = 255): 최대 길이 255자 (선택적 필드)
     */
    @Column(length = 255)
    private String message;
}


