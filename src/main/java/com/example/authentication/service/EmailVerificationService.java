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

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
	private final EmailVerificationTokenRepository tokenRepository;
	private final UserRepository userRepository;
	private final ObjectProvider<JavaMailSender> mailSenderProvider;
	@Value("${app.email.verification.expire-seconds:1800}")
	private long expireSeconds;
	@Value("${app.email.verification.base-url:http://localhost:8080/api/auth/verify}")
	private String baseUrl;
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

	@Transactional
	public void sendVerificationEmail(User user) {
		String token = UUID.randomUUID().toString().replace("-", "");
		EmailVerificationToken evt = EmailVerificationToken.builder()
				.user(user)
				.token(token)
				.expiresAt(Instant.now().plusSeconds(expireSeconds))
				.used(false)
				.build();
		tokenRepository.save(evt);
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (!emailEnabled || mailSender == null) {
			log.info("Email sending disabled or MailSender unavailable. Verification link: {}?token={}", baseUrl, token);
			return;
		}
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(user.getEmail());
		message.setSubject("[Authentication] 이메일 인증을 완료해주세요");
		message.setText("아래 링크를 클릭하여 인증을 완료하세요: " + baseUrl + "?token=" + token);
		mailSender.send(message);
	}

	@Transactional
	public void verify(String token) {
		EmailVerificationToken evt = tokenRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("잘못된 토큰입니다."));
		if (evt.isUsed() || evt.getExpiresAt().isBefore(Instant.now())) {
			throw new IllegalStateException("토큰이 만료되었거나 이미 사용되었습니다.");
		}
		User user = evt.getUser();
		user.markEmailVerified();
		userRepository.save(user);
		evt.markUsed();
		tokenRepository.save(evt);
	}
}
