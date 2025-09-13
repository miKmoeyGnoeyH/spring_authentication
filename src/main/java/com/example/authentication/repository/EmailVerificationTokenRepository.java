package com.example.authentication.repository;

import com.example.authentication.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 이메일 인증 토큰(EmailVerificationToken) 데이터 접근을 위한 Repository 인터페이스
 * 
 * 이 인터페이스는 EmailVerificationToken 엔티티에 대한 데이터베이스 작업을 수행합니다.
 * Spring Data JPA가 자동으로 구현체를 생성하여 빈으로 등록합니다.
 * 
 * 이메일 인증 토큰 관리 기능:
 * - 토큰 생성, 조회, 수정, 삭제
 * - 토큰 문자열로 조회
 * - 만료된 토큰 정리
 */
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    
    /**
     * 토큰 문자열로 이메일 인증 토큰을 조회하는 메서드
     * 
     * Spring Data JPA가 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.
     * "findBy" + "Token" → "SELECT * FROM email_verification_tokens WHERE token = ?" 쿼리 생성
     * 
     * @param token 조회할 토큰 문자열 (이메일 링크에서 추출한 토큰)
     * @return Optional<EmailVerificationToken> - 토큰이 존재하면 EmailVerificationToken 객체, 없으면 Optional.empty()
     * 
     * 사용 예시:
     * - 사용자가 이메일 인증 링크를 클릭했을 때 토큰 검증
     * - 토큰 유효성 확인
     */
    Optional<EmailVerificationToken> findByToken(String token);
}


