package com.example.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot 애플리케이션의 메인 클래스
 * 
 * 이 클래스는 Spring Boot 애플리케이션의 시작점입니다.
 * main 메서드가 실행되면 Spring Boot가 자동으로 애플리케이션을 구성하고 실행합니다.
 * 
 * 주요 어노테이션 설명:
 * - @SpringBootApplication: Spring Boot의 핵심 어노테이션으로, 다음 기능들을 자동으로 활성화합니다:
 *   1. @Configuration: 이 클래스를 설정 클래스로 인식
 *   2. @EnableAutoConfiguration: Spring Boot의 자동 설정 활성화
 *   3. @ComponentScan: 지정된 패키지와 하위 패키지에서 컴포넌트들을 자동으로 스캔하여 빈으로 등록
 * 
 * - @EnableJpaRepositories: JPA Repository들을 활성화하는 어노테이션
 *   basePackages로 지정된 패키지에서 Repository 인터페이스들을 찾아서 자동으로 구현체를 생성합니다.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.authentication.repository")
public class AuthenticationApplication {

	/**
	 * 애플리케이션의 진입점(Entry Point)
	 * 
	 * 이 메서드가 실행되면:
	 * 1. Spring Boot가 자동으로 애플리케이션 컨텍스트(ApplicationContext)를 생성
	 * 2. 설정된 모든 빈(Bean)들을 생성하고 의존성을 주입
	 * 3. 내장된 웹 서버(Tomcat)를 시작하여 HTTP 요청을 받을 수 있도록 준비
	 * 4. 애플리케이션이 완전히 시작될 때까지 대기
	 * 
	 * @param args 명령행 인수 (예: --server.port=8080)
	 */
	public static void main(String[] args) {
		// SpringApplication.run()이 실제로 애플리케이션을 시작하는 메서드입니다.
		// 첫 번째 인수: 메인 클래스 (설정의 기준점이 됨)
		// 두 번째 인수: 명령행 인수
		SpringApplication.run(AuthenticationApplication.class, args);
	}

}
