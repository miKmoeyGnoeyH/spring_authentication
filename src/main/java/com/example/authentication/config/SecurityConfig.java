package com.example.authentication.config;

import com.example.authentication.repository.RoleRepository;
import com.example.authentication.repository.UserRepository;
import com.example.authentication.repository.SocialAccountRepository;
import com.example.authentication.security.JwtAuthenticationFilter;
import com.example.authentication.security.JwtService;
import com.example.authentication.security.OAuth2LoginSuccessHandler;
import com.example.authentication.security.RefreshTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Spring Security 설정 클래스
 * 
 * 이 클래스는 애플리케이션의 보안 설정을 구성합니다.
 * JWT 기반 인증, OAuth2 소셜 로그인, 비밀번호 암호화 등을 설정합니다.
 * 
 * 주요 설정:
 * - JWT 기반 인증 필터 설정
 * - OAuth2 소셜 로그인 설정 (선택적)
 * - 비밀번호 암호화 설정
 * - URL별 접근 권한 설정
 * - 메서드 레벨 보안 활성화
 * 
 * @Configuration: 이 클래스를 Spring 설정 클래스로 등록합니다.
 * @EnableWebSecurity: Spring Security 웹 보안을 활성화합니다.
 * @EnableMethodSecurity: 메서드 레벨 보안(@PreAuthorize 등)을 활성화합니다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	/**
	 * 비밀번호 암호화를 위한 PasswordEncoder 빈
	 * 
	 * BCrypt 알고리즘을 사용하여 비밀번호를 해시화합니다.
	 * BCrypt는 솔트(salt)를 자동으로 생성하여 같은 비밀번호라도 다른 해시값을 생성합니다.
	 * 
	 * @return BCryptPasswordEncoder 인스턴스
	 * 
	 * BCrypt 특징:
	 * - 솔트 자동 생성으로 레인보우 테이블 공격 방지
	 * - 적응형 해시 함수로 시간이 지나도 보안 강도 조정 가능
	 * - Spring Security에서 권장하는 기본 암호화 방식
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Spring Security 필터 체인을 구성하는 빈
	 * 
	 * 이 메서드는 HTTP 요청에 대한 보안 필터 체인을 설정합니다.
	 * JWT 인증, OAuth2 로그인, URL별 접근 권한 등을 구성합니다.
	 * 
	 * @param http HttpSecurity 빌더 객체
	 * @param jwtService JWT 토큰 처리 서비스
	 * @param refreshTokenService 갱신 토큰 관리 서비스
	 * @param userRepository 사용자 데이터 접근 Repository
	 * @param roleRepository 역할 데이터 접근 Repository
	 * @param socialAccountRepository 소셜 계정 데이터 접근 Repository
	 * @param clientRegistrationRepositoryProvider OAuth2 클라이언트 등록 정보 제공자 (선택적)
	 * @return SecurityFilterChain 구성된 보안 필터 체인
	 * @throws Exception 설정 중 오류 발생 시
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService, RefreshTokenService refreshTokenService, UserRepository userRepository, RoleRepository roleRepository, SocialAccountRepository socialAccountRepository, ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider) throws Exception {
		http
				// CSRF 보호 비활성화 (JWT 기반 인증에서는 불필요)
				.csrf(csrf -> csrf.disable())
				
				// 세션을 사용하지 않는 stateless 방식으로 설정
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				
				// URL별 접근 권한 설정
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/**").permitAll()        // Spring Actuator 엔드포인트는 모든 사용자 접근 허용
						.requestMatchers("/api/auth/**", "/oauth2/**").permitAll()  // 인증 관련 엔드포인트는 모든 사용자 접근 허용
						.anyRequest().authenticated()                       // 그 외 모든 요청은 인증 필요
				)
				
				// JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
				.addFilterBefore(new JwtAuthenticationFilter(jwtService, refreshTokenService, userRepository), UsernamePasswordAuthenticationFilter.class)
				
				// HTTP Basic 인증 활성화 (선택적)
				.httpBasic(Customizer.withDefaults());

		// OAuth2 클라이언트 등록 정보가 있는 경우에만 OAuth2 로그인 활성화
		ClientRegistrationRepository clientRegistrationRepository = clientRegistrationRepositoryProvider.getIfAvailable();
		if (clientRegistrationRepository != null) {
			http.oauth2Login(oauth -> oauth
					.successHandler(new OAuth2LoginSuccessHandler(userRepository, roleRepository, socialAccountRepository, jwtService, refreshTokenService))
			);
		}
		
		return http.build();
	}
}


