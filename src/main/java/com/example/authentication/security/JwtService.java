package com.example.authentication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT(JSON Web Token) 서비스 클래스
 * 
 * 이 클래스는 JWT 토큰의 생성, 파싱, 검증을 담당합니다.
 * 액세스 토큰과 갱신 토큰을 각각 다른 키로 서명하여 보안을 강화합니다.
 * 
 * 주요 기능:
 * - 액세스 토큰 생성 및 검증 (짧은 수명)
 * - 갱신 토큰 생성 및 검증 (긴 수명)
 * - 토큰 파싱 및 클레임 추출
 * - 토큰 유효성 검사
 * 
 * JWT 구조:
 * - Header: 토큰 타입과 서명 알고리즘 정보
 * - Payload: 사용자 정보와 메타데이터 (클레임)
 * - Signature: 토큰의 무결성을 보장하는 서명
 * 
 * @Service: 이 클래스를 Spring의 서비스 빈으로 등록합니다.
 */
@Service
public class JwtService {
	
	/**
	 * 액세스 토큰 서명을 위한 비밀키
	 * 
	 * 액세스 토큰을 서명하고 검증하는 데 사용됩니다.
	 * HMAC-SHA256 알고리즘을 사용합니다.
	 */
	private final SecretKey accessKey;
	
	/**
	 * 갱신 토큰 서명을 위한 비밀키
	 * 
	 * 갱신 토큰을 서명하고 검증하는 데 사용됩니다.
	 * 액세스 토큰과 다른 키를 사용하여 보안을 강화합니다.
	 */
	private final SecretKey refreshKey;
	
	/**
	 * 액세스 토큰 수명 (초)
	 * 
	 * 액세스 토큰이 유효한 시간을 초 단위로 저장합니다.
	 * 기본값: 900초 (15분)
	 */
	private final long accessTtlSeconds;
	
	/**
	 * 갱신 토큰 수명 (초)
	 * 
	 * 갱신 토큰이 유효한 시간을 초 단위로 저장합니다.
	 * 기본값: 604800초 (7일)
	 */
	private final long refreshTtlSeconds;

	/**
	 * JWT 서비스 생성자
	 * 
	 * @Value 어노테이션을 통해 application.properties에서 설정값을 주입받습니다.
	 * 
	 * @param accessSecret 액세스 토큰 서명용 비밀키 (기본값: "access-secret-please-change")
	 * @param refreshSecret 갱신 토큰 서명용 비밀키 (기본값: "refresh-secret-please-change")
	 * @param accessTtlSeconds 액세스 토큰 수명 (기본값: 900초)
	 * @param refreshTtlSeconds 갱신 토큰 수명 (기본값: 604800초)
	 */
	public JwtService(
			@Value("${app.jwt.access-secret:access-secret-please-change}") String accessSecret,
			@Value("${app.jwt.refresh-secret:refresh-secret-please-change}") String refreshSecret,
			@Value("${app.jwt.access-ttl-seconds:900}") long accessTtlSeconds,
			@Value("${app.jwt.refresh-ttl-seconds:604800}") long refreshTtlSeconds
	) {
		// 문자열 비밀키를 HMAC-SHA256용 SecretKey로 변환
		this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
		this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
		this.accessTtlSeconds = accessTtlSeconds;
		this.refreshTtlSeconds = refreshTtlSeconds;
	}

	/**
	 * 액세스 토큰을 생성하는 메서드
	 * 
	 * API 요청 시 인증을 위해 사용되는 짧은 수명의 JWT 토큰을 생성합니다.
	 * 
	 * @param subject 토큰의 주체 (보통 사용자 ID)
	 * @param claims 토큰에 포함할 추가 정보 (사용자 역할, 권한 등)
	 * @return String 생성된 액세스 토큰 (JWT 문자열)
	 * 
	 * 토큰 구성 요소:
	 * - jti (JWT ID): 토큰의 고유 식별자 (UUID)
	 * - sub (Subject): 토큰의 주체 (사용자 ID)
	 * - iat (Issued At): 토큰 발급 시간
	 * - exp (Expiration): 토큰 만료 시간
	 * - claims: 추가 클레임 정보
	 */
	public String generateAccessToken(String subject, Map<String, Object> claims) {
		Instant now = Instant.now();
		return Jwts.builder()
				.id(UUID.randomUUID().toString())                    // JWT ID (고유 식별자)
				.subject(subject)                                     // 주체 (사용자 ID)
				.issuedAt(Date.from(now))                            // 발급 시간
				.expiration(Date.from(now.plusSeconds(accessTtlSeconds)))  // 만료 시간
				.claims(claims)                                       // 추가 클레임
				.signWith(accessKey)                                  // 액세스 토큰 키로 서명
				.compact();                                           // JWT 문자열로 변환
	}

	/**
	 * 갱신 토큰을 생성하는 메서드
	 * 
	 * 액세스 토큰이 만료되었을 때 새로운 액세스 토큰을 발급받기 위해 사용되는 긴 수명의 JWT 토큰을 생성합니다.
	 * 
	 * @param subject 토큰의 주체 (보통 사용자 ID)
	 * @param claims 토큰에 포함할 추가 정보
	 * @return String 생성된 갱신 토큰 (JWT 문자열)
	 * 
	 * 보안 특징:
	 * - 액세스 토큰과 다른 키로 서명되어 분리된 보안을 제공
	 * - 긴 수명을 가지지만 서버에서 관리되어 필요시 무효화 가능
	 */
	public String generateRefreshToken(String subject, Map<String, Object> claims) {
		Instant now = Instant.now();
		return Jwts.builder()
				.id(UUID.randomUUID().toString())                    // JWT ID (고유 식별자)
				.subject(subject)                                     // 주체 (사용자 ID)
				.issuedAt(Date.from(now))                            // 발급 시간
				.expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))  // 만료 시간
				.claims(claims)                                       // 추가 클레임
				.signWith(refreshKey)                                 // 갱신 토큰 키로 서명
				.compact();                                           // JWT 문자열로 변환
	}

	/**
	 * 액세스 토큰을 파싱하여 클레임을 추출하는 메서드
	 * 
	 * @param token 파싱할 액세스 토큰 문자열
	 * @return Claims 토큰에서 추출된 클레임 정보
	 * @throws io.jsonwebtoken.JwtException 토큰이 유효하지 않거나 서명이 잘못된 경우
	 */
	public Claims parseAccessClaims(String token) {
		return Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token).getPayload();
	}

	/**
	 * 갱신 토큰을 파싱하여 클레임을 추출하는 메서드
	 * 
	 * @param token 파싱할 갱신 토큰 문자열
	 * @return Claims 토큰에서 추출된 클레임 정보
	 * @throws io.jsonwebtoken.JwtException 토큰이 유효하지 않거나 서명이 잘못된 경우
	 */
	public Claims parseRefreshClaims(String token) {
		return Jwts.parser().verifyWith(refreshKey).build().parseSignedClaims(token).getPayload();
	}

	/**
	 * 액세스 토큰에서 주체(사용자 ID)를 추출하는 메서드
	 * 
	 * @param token 액세스 토큰 문자열
	 * @return String 토큰의 주체 (사용자 ID)
	 */
	public String getSubjectFromAccess(String token) {
		return parseAccessClaims(token).getSubject();
	}

	/**
	 * 액세스 토큰의 유효성을 검사하는 메서드
	 * 
	 * 토큰이 유효한 형식이고 서명이 올바른지 확인합니다.
	 * 만료 여부는 별도로 확인해야 합니다.
	 * 
	 * @param token 검사할 액세스 토큰 문자열
	 * @return boolean 토큰이 유효하면 true, 그렇지 않으면 false
	 * 
	 * 검사 항목:
	 * - 토큰 형식이 올바른지
	 * - 서명이 유효한지
	 * - 파싱이 가능한지
	 */
	public boolean isAccessTokenValid(String token) {
		try {
			parseAccessClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
