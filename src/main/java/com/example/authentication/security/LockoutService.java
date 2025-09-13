package com.example.authentication.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 로그인 잠금 서비스 클래스
 * 
 * 이 클래스는 로그인 실패 횟수를 추적하고 일정 횟수 이상 실패 시 계정을 잠그는 기능을 제공합니다.
 * 브루트 포스 공격을 방지하고 계정 보안을 강화합니다.
 * 
 * 주요 기능:
 * - 로그인 실패 횟수 추적
 * - 최대 실패 횟수 초과 시 계정 잠금
 * - 잠금 상태 확인 및 해제
 * - 실패 횟수 초기화
 * 
 * 보안 특징:
 * - 시간 윈도우 내에서의 실패 횟수만 카운트
 * - 잠금 시간이 지나면 자동 해제
 * - Redis 또는 메모리 저장소 지원
 * 
 * @Service: 이 클래스를 Spring의 서비스 빈으로 등록합니다.
 */
@Service
public class LockoutService {
	
	/**
	 * Redis 템플릿 제공자 (선택적)
	 * 
	 * Redis가 설정된 경우 Redis를 사용하고, 그렇지 않으면 메모리 저장소를 사용합니다.
	 */
	private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
	
	/**
	 * 최대 허용 실패 횟수
	 * 
	 * 이 횟수를 초과하면 계정이 잠깁니다.
	 * 기본값: 5회
	 */
	private final int maxFailures;
	
	/**
	 * 실패 횟수 카운트 윈도우
	 * 
	 * 이 시간 내에서의 실패 횟수만 카운트합니다.
	 * 기본값: 900초 (15분)
	 */
	private final Duration failureWindow;
	
	/**
	 * 계정 잠금 지속 시간
	 * 
	 * 계정이 잠긴 후 이 시간 동안 로그인이 차단됩니다.
	 * 기본값: 900초 (15분)
	 */
	private final Duration lockoutDuration;
	
	/**
	 * 메모리 기반 실패 횟수 저장소 (Redis 대안)
	 * 
	 * Redis가 사용 불가능한 경우 실패 횟수를 메모리에 저장합니다.
	 * ConcurrentHashMap을 사용하여 스레드 안전성을 보장합니다.
	 */
	private final Map<String, Integer> failureCounts = new ConcurrentHashMap<>();
	
	/**
	 * 메모리 기반 잠금 시간 저장소 (Redis 대안)
	 * 
	 * Redis가 사용 불가능한 경우 잠금 시간을 메모리에 저장합니다.
	 * 키: principalKey, 값: 잠금 시작 시간 (밀리초)
	 */
	private final Map<String, Long> lockTimes = new ConcurrentHashMap<>();

	/**
	 * 잠금 서비스 생성자
	 * 
	 * @param redisTemplateProvider Redis 템플릿 제공자 (선택적)
	 * @param maxFailures 최대 허용 실패 횟수 (기본값: 5)
	 * @param failureWindowSeconds 실패 횟수 카운트 윈도우 (초, 기본값: 900)
	 * @param lockoutSeconds 계정 잠금 지속 시간 (초, 기본값: 900)
	 */
	public LockoutService(ObjectProvider<StringRedisTemplate> redisTemplateProvider,
	                     @Value("${app.lockout.max-failures:5}") int maxFailures,
	                     @Value("${app.lockout.failure-window-seconds:900}") long failureWindowSeconds,
	                     @Value("${app.lockout.lockout-seconds:900}") long lockoutSeconds) {
		this.redisTemplateProvider = redisTemplateProvider;
		this.maxFailures = maxFailures;
		this.failureWindow = Duration.ofSeconds(failureWindowSeconds);
		this.lockoutDuration = Duration.ofSeconds(lockoutSeconds);
	}

	/**
	 * 계정이 잠금 상태인지 확인하는 메서드
	 * 
	 * @param principalKey 잠금 상태를 확인할 주체 키 (보통 이메일 주소)
	 * @return boolean 계정이 잠금 상태이면 true, 그렇지 않으면 false
	 */
	public boolean isLocked(String principalKey) {
		StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
		if (redis != null) {
			// Redis에서 잠금 상태 확인
			String key = keyLock(principalKey);
			return Boolean.TRUE.equals(redis.hasKey(key));
		} else {
			// 메모리에서 잠금 상태 확인
			Long lockTime = lockTimes.get(principalKey);
			if (lockTime != null) {
				// 잠금 시간이 지났으면 해제
				if (System.currentTimeMillis() - lockTime > lockoutDuration.toMillis()) {
					lockTimes.remove(principalKey);
					return false;
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * 로그인 실패를 기록하는 메서드
	 * 
	 * 실패 횟수를 증가시키고, 최대 허용 횟수를 초과하면 계정을 잠급니다.
	 * 
	 * @param principalKey 실패를 기록할 주체 키 (보통 이메일 주소)
	 */
	public void recordFailure(String principalKey) {
		StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
		if (redis != null) {
			// Redis에서 실패 횟수 증가
			String failKey = keyFail(principalKey);
			Long count = redis.opsForValue().increment(failKey);
			if (count != null && count == 1) {
				// 첫 번째 실패 시 TTL 설정
				redis.expire(failKey, failureWindow);
			}
			if (count != null && count >= maxFailures) {
				// 최대 실패 횟수 초과 시 계정 잠금
				lock(principalKey);
			}
		} else {
			// 메모리 기반 실패 카운트
			int count = failureCounts.merge(principalKey, 1, Integer::sum);
			if (count >= maxFailures) {
				// 최대 실패 횟수 초과 시 계정 잠금
				lock(principalKey);
			}
		}
	}

	/**
	 * 실패 횟수를 초기화하는 메서드
	 * 
	 * 로그인 성공 시 호출되어 실패 횟수를 0으로 리셋합니다.
	 * 
	 * @param principalKey 초기화할 주체 키 (보통 이메일 주소)
	 */
	public void resetFailures(String principalKey) {
		StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
		if (redis != null) {
			// Redis에서 실패 횟수 삭제
			redis.delete(keyFail(principalKey));
		} else {
			// 메모리에서 실패 횟수 삭제
			failureCounts.remove(principalKey);
		}
	}

	/**
	 * 계정을 잠그는 메서드
	 * 
	 * @param principalKey 잠글 주체 키 (보통 이메일 주소)
	 */
	public void lock(String principalKey) {
		StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
		if (redis != null) {
			// Redis에 잠금 상태 저장 (TTL과 함께)
			redis.opsForValue().set(keyLock(principalKey), "1", lockoutDuration);
		} else {
			// 메모리에 잠금 시간 저장
			lockTimes.put(principalKey, System.currentTimeMillis());
		}
	}

	/**
	 * 실패 횟수 키를 생성하는 메서드
	 * 
	 * @param principalKey 주체 키
	 * @return String "login:fail:{principalKey}" 형태의 키
	 */
	private String keyFail(String principalKey) {
		return "login:fail:" + principalKey;
	}

	/**
	 * 잠금 상태 키를 생성하는 메서드
	 * 
	 * @param principalKey 주체 키
	 * @return String "login:lock:{principalKey}" 형태의 키
	 */
	private String keyLock(String principalKey) {
		return "login:lock:" + principalKey;
	}
}
