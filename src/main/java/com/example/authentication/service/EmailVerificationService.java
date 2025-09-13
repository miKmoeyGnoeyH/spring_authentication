package com.example.authentication.service;

import com.example.authentication.domain.EmailVerificationToken;
import com.example.authentication.domain.User;
import com.example.authentication.repository.EmailVerificationTokenRepository;
import com.example.authentication.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.UUID;

/**
 * 이메일 인증 서비스 클래스
 * 
 * 이 클래스는 사용자의 이메일 인증과 관련된 비즈니스 로직을 처리합니다.
 * 회원가입 시 이메일 인증 토큰을 생성하고 이메일을 발송하며,
 * 사용자가 인증 링크를 클릭했을 때 토큰을 검증합니다.
 * 
 * 주요 기능:
 * - 이메일 인증 토큰 생성 및 발송
 * - 토큰 검증 및 사용자 이메일 인증 완료 처리
 * - 메일 발송 기능 (선택적)
 * 
 * @Service: 이 클래스를 Spring의 서비스 빈으로 등록합니다.
 * @RequiredArgsConstructor: final 필드들에 대한 생성자를 자동 생성합니다.
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    
    /**
     * 로깅을 위한 Logger 인스턴스
     * 
     * 로그를 통해 이메일 발송 상태나 오류를 추적할 수 있습니다.
     */
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    
    /**
     * 이메일 인증 토큰 데이터 접근을 위한 Repository
     * 
     * 토큰의 생성, 조회, 수정, 삭제 작업을 수행합니다.
     */
	private final EmailVerificationTokenRepository tokenRepository;
	
	/**
	 * 사용자 데이터 접근을 위한 Repository
	 * 
	 * 사용자의 이메일 인증 상태를 업데이트하는 데 사용됩니다.
	 */
	private final UserRepository userRepository;
	
	/**
	 * 메일 발송을 위한 JavaMailSender (선택적)
	 * 
	 * ObjectProvider를 사용하여 메일 설정이 없을 때도 서비스가 정상 동작하도록 합니다.
	 * 메일 서버가 설정되지 않은 경우 null이 될 수 있습니다.
	 */
	private final ObjectProvider<JavaMailSender> mailSenderProvider;
	
	/**
	 * 이메일 인증 토큰 만료 시간 (초)
	 * 
	 * @Value: application.properties에서 설정값을 주입받습니다.
	 * 기본값: 1800초 (30분)
	 */
	@Value("${app.email.verification.expire-seconds:1800}")
	private long expireSeconds;
	
	/**
	 * 이메일 인증 링크의 기본 URL
	 * 
	 * @Value: application.properties에서 설정값을 주입받습니다.
	 * 기본값: http://localhost:8080/api/auth/verify
	 */
	@Value("${app.email.verification.base-url:http://localhost:8080/api/auth/verify}")
	private String baseUrl;
	
	/**
	 * 이메일 발송 기능 활성화 여부
	 * 
	 * @Value: application.properties에서 설정값을 주입받습니다.
	 * 기본값: false (개발 환경에서는 비활성화)
	 */
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

	/**
	 * 사용자에게 이메일 인증 메일을 발송하는 메서드
	 * 
	 * 이 메서드는 다음과 같은 작업을 수행합니다:
	 * 1. 랜덤한 인증 토큰을 생성
	 * 2. 토큰을 데이터베이스에 저장 (만료 시간 포함)
	 * 3. 사용자에게 인증 링크가 포함된 이메일 발송
	 * 
	 * @Transactional: 이 메서드가 트랜잭션 내에서 실행됩니다.
	 * 데이터베이스 작업이 실패하면 모든 변경사항이 롤백됩니다.
	 * 
	 * @param user 이메일 인증을 받을 사용자 객체
	 */
	@Transactional
	public void sendVerificationEmail(User user) {
		// 1. 랜덤한 토큰 생성 (UUID에서 하이픈 제거)
		String token = UUID.randomUUID().toString().replace("-", "");
		
		// 2. 이메일 인증 토큰 엔티티 생성
		EmailVerificationToken evt = EmailVerificationToken.builder()
				.user(user)                                    // 토큰을 받을 사용자
				.token(token)                                  // 생성된 토큰
				.expiresAt(Instant.now().plusSeconds(expireSeconds))  // 만료 시간 (현재 시간 + 설정된 초)
				.used(false)                                   // 아직 사용되지 않음
				.build();
		
		// 3. 토큰을 데이터베이스에 저장
		tokenRepository.save(evt);
		
		// 4. 메일 발송 기능 확인
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (!emailEnabled || mailSender == null) {
			// 메일 발송이 비활성화되었거나 메일 서버가 없는 경우
			// 로그에 인증 링크를 출력 (개발 환경에서 사용)
			log.info("Email sending disabled or MailSender unavailable. Verification link: {}?token={}", baseUrl, token);
			return;
		}
		
		// 5. 이메일 메시지 생성 및 발송
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(user.getEmail());                        // 수신자 이메일
		message.setSubject("[Authentication] 이메일 인증을 완료해주세요");  // 이메일 제목
		message.setText("아래 링크를 클릭하여 인증을 완료하세요: " + baseUrl + "?token=" + token);  // 이메일 내용
		mailSender.send(message);  // 이메일 발송
	}

	/**
	 * 이메일 인증 토큰을 검증하고 사용자의 이메일을 인증 완료로 처리하는 메서드
	 * 
	 * 이 메서드는 다음과 같은 작업을 수행합니다:
	 * 1. 토큰이 데이터베이스에 존재하는지 확인
	 * 2. 토큰이 만료되었거나 이미 사용되었는지 확인
	 * 3. 사용자의 이메일 인증 상태를 완료로 변경
	 * 4. 토큰을 사용됨으로 표시
	 * 
	 * @Transactional: 이 메서드가 트랜잭션 내에서 실행됩니다.
	 * 데이터베이스 작업이 실패하면 모든 변경사항이 롤백됩니다.
	 * 
	 * @param token 검증할 토큰 문자열
	 * @throws IllegalArgumentException 토큰이 존재하지 않는 경우
	 * @throws IllegalStateException 토큰이 만료되었거나 이미 사용된 경우
	 */
	@Transactional
	public void verify(String token) {
		// 1. 토큰으로 이메일 인증 토큰 조회
		EmailVerificationToken evt = tokenRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("잘못된 토큰입니다."));
		
		// 2. 토큰 유효성 검사
		if (evt.isUsed() || evt.getExpiresAt().isBefore(Instant.now())) {
			throw new IllegalStateException("토큰이 만료되었거나 이미 사용되었습니다.");
		}
		
		// 3. 사용자의 이메일 인증 상태를 완료로 변경
		User user = evt.getUser();
		user.markEmailVerified();  // 이메일 인증 완료로 표시
		userRepository.save(user);  // 변경사항 저장
		
		// 4. 토큰을 사용됨으로 표시 (재사용 방지)
		evt.markUsed();
		tokenRepository.save(evt);  // 변경사항 저장
	}
}
