package com.example.authentication.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 갱신 토큰 관리 서비스 클래스
 * 
 * 이 클래스는 갱신 토큰의 저장, 검증, 무효화를 담당합니다.
 * Redis가 사용 가능한 경우 Redis를 사용하고, 그렇지 않으면 메모리 저장소를 사용합니다.
 * 
 * 주요 기능:
 * - 갱신 토큰 저장 (사용자별, JTI별)
 * - 갱신 토큰 존재 여부 확인
 * - 갱신 토큰 무효화 (블랙리스트)
 * - 토큰 만료 시간 관리
 * 
 * 보안 특징:
 * - 갱신 토큰은 서버에서 관리되어 필요시 무효화 가능
 * - JTI(JWT ID)를 사용하여 토큰을 고유하게 식별
 * - 토큰 순환을 통한 보안 강화
 * 
 * @Service: 이 클래스를 Spring의 서비스 빈으로 등록합니다.
 */
@Service
public class RefreshTokenService {
	
	/**
	 * Redis 템플릿 제공자 (선택적)
	 * 
	 * Redis가 설정된 경우 Redis를 사용하고, 그렇지 않으면 메모리 저장소를 사용합니다.
	 * ObjectProvider를 사용하여 Redis가 없어도 서비스가 정상 동작하도록 합니다.
	 */
	private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
	
	/**
	 * 메모리 저장소 (Redis 대안)
	 * 
	 * Redis가 사용 불가능한 경우 갱신 토큰을 메모리에 저장합니다.
	 * ConcurrentHashMap을 사용하여 스레드 안전성을 보장합니다.
	 * 
	 * 주의사항: 실제 운영 환경에서는 Redis나 다른 영구 저장소를 사용해야 합니다.
	 */
	private final Map<String, String> inMemoryStore = new ConcurrentHashMap<>();

	/**
	 * 갱신 토큰 서비스 생성자
	 * 
	 * @param redisTemplateProvider Redis 템플릿 제공자 (선택적)
	 */
	public RefreshTokenService(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
		this.redisTemplateProvider = redisTemplateProvider;
	}

	/**
	 * 갱신 토큰을 저장하는 메서드
	 * 
	 * 사용자 ID와 JTI를 조합한 키로 갱신 토큰을 저장합니다.
	 * Redis가 사용 가능한 경우 Redis에 저장하고, 그렇지 않으면 메모리에 저장합니다.
	 * 
	 * @param userId 사용자 ID
	 * @param jti JWT ID (토큰의 고유 식별자)
	 * @param token 저장할 갱신 토큰 문자열
	 * @param ttl 토큰 만료 시간
	 */
	public void store(String userId, String jti, String token, Duration ttl) {
		String key = keyUserRefresh(userId, jti);
		StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
		if (redis != null) {
			// Redis에 토큰 저장 (TTL과 함께)
			redis.opsForValue().set(key, token, ttl);
		} else {
			// Redis가 없으면 메모리에 저장 (실제 운영에서는 적절한 대안 필요)
			inMemoryStore.put(key, token);
		}
	}

	/**
	 * 갱신 토큰이 존재하는지 확인하는 메서드
	 * 
	 * @param userId 사용자 ID
	 * @param jti JWT ID (토큰의 고유 식별자)
	 * @return boolean 토큰이 존재하면 true, 그렇지 않으면 false
	 */
	public boolean exists(String userId, String jti) {
		String key = keyUserRefresh(userId, jti);
		StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
		if (redis != null) {
			return Boolean.TRUE.equals(redis.hasKey(key));
		} else {
			return inMemoryStore.containsKey(key);
		}
	}

	/**
	 * 갱신 토큰이 무효화(블랙리스트)되었는지 확인하는 메서드
	 * 
	 * @param jti JWT ID (토큰의 고유 식별자)
	 * @return boolean 토큰이 무효화되었으면 true, 그렇지 않으면 false
	 */
	public boolean isRevoked(String jti) {
		String key = keyBlacklist(jti);
		StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
		if (redis != null) {
			return Boolean.TRUE.equals(redis.hasKey(key));
		} else {
			return inMemoryStore.containsKey(key);
		}
	}

	/**
	 * 갱신 토큰을 무효화하는 메서드
	 * 
	 * 토큰을 블랙리스트에 추가하여 더 이상 사용할 수 없도록 합니다.
	 * 
	 * @param jti 무효화할 JWT ID
	 * @param ttl 블랙리스트 유지 시간
	 */
	public void revoke(String jti, Duration ttl) {
		String key = keyBlacklist(jti);
		StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
		if (redis != null) {
			// Redis에 블랙리스트 항목 저장
			redis.opsForValue().set(key, "1", ttl);
		} else {
			// Redis가 없으면 메모리에 저장
			inMemoryStore.put(key, "1");
		}
	}

	/**
	 * 사용자 갱신 토큰을 위한 키를 생성하는 메서드
	 * 
	 * @param userId 사용자 ID
	 * @param jti JWT ID
	 * @return String "refresh:{userId}:{jti}" 형태의 키
	 */
	private String keyUserRefresh(String userId, String jti) {
		return "refresh:" + userId + ":" + jti;
	}

	/**
	 * 블랙리스트 항목을 위한 키를 생성하는 메서드
	 * 
	 * @param jti JWT ID
	 * @return String "blacklist:{jti}" 형태의 키
	 */
	private String keyBlacklist(String jti) {
		return "blacklist:" + jti;
	}
}
