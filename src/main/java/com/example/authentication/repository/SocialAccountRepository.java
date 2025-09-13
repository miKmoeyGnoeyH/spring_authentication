package com.example.authentication.repository;

import com.example.authentication.domain.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 소셜 계정 연결(SocialAccount) 데이터 접근을 위한 Repository 인터페이스
 * 
 * 이 인터페이스는 SocialAccount 엔티티에 대한 데이터베이스 작업을 수행합니다.
 * Spring Data JPA가 자동으로 구현체를 생성하여 빈으로 등록합니다.
 * 
 * 소셜 계정 연결 관리 기능:
 * - 소셜 계정 연결 생성, 조회, 수정, 삭제
 * - 제공자와 제공자 사용자 ID로 조회
 * - OAuth2 로그인 처리
 */
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    
    /**
     * 제공자와 제공자 사용자 ID로 소셜 계정을 조회하는 메서드
     * 
     * Spring Data JPA가 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.
     * "findBy" + "Provider" + "And" + "ProviderUserId" 
     * → "SELECT * FROM social_accounts WHERE provider = ? AND provider_user_id = ?" 쿼리 생성
     * 
     * @param provider 소셜 로그인 제공자 (예: "google", "facebook", "github")
     * @param providerUserId 제공자에서의 사용자 ID (예: Google의 경우 "1234567890abcdef")
     * @return Optional<SocialAccount> - 소셜 계정이 존재하면 SocialAccount 객체, 없으면 Optional.empty()
     * 
     * 사용 예시:
     * - OAuth2 로그인 시 기존 연결된 계정 확인
     * - 소셜 계정으로 로그인한 사용자 식별
     * - 계정 연결 상태 확인
     */
    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
}


