package com.example.authentication.config;

import com.example.authentication.domain.Role;
import com.example.authentication.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 데이터 초기화 클래스
 * 
 * 이 클래스는 애플리케이션 시작 시 필요한 기본 데이터를 데이터베이스에 초기화합니다.
 * ApplicationRunner를 구현하여 Spring Boot 애플리케이션이 완전히 시작된 후 실행됩니다.
 * 
 * 주요 기능:
 * - 기본 역할(USER, MANAGER, ADMIN) 자동 생성
 * - 중복 생성 방지 (이미 존재하는 역할은 생성하지 않음)
 * - 트랜잭션 보장으로 데이터 일관성 유지
 * 
 * @Configuration: 이 클래스를 Spring 설정 클래스로 등록합니다.
 * @RequiredArgsConstructor: final 필드들에 대한 생성자를 자동 생성합니다.
 * ApplicationRunner: Spring Boot 애플리케이션 시작 후 실행되는 인터페이스입니다.
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    
    /**
     * 로깅을 위한 Logger 인스턴스
     * 
     * 초기화 과정에서 생성된 역할들을 로그로 기록합니다.
     */
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    /**
     * 역할 데이터 접근을 위한 Repository
     * 
     * 역할의 존재 여부 확인 및 생성에 사용됩니다.
     */
    private final RoleRepository roleRepository;

    /**
     * 애플리케이션 시작 후 실행되는 초기화 메서드
     * 
     * 기본 역할들(USER, MANAGER, ADMIN)이 데이터베이스에 존재하지 않으면 생성합니다.
     * 
     * @Transactional: 이 메서드가 트랜잭션 내에서 실행됩니다.
     * 데이터베이스 작업이 실패하면 모든 변경사항이 롤백됩니다.
     * 
     * @param args 애플리케이션 시작 인수 (사용하지 않음)
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 1. 생성할 기본 역할 목록 정의
        List<String> baseRoles = List.of("USER", "MANAGER", "ADMIN");
        
        // 2. 각 역할에 대해 존재 여부 확인 후 생성
        for (String roleName : baseRoles) {
            if (!roleRepository.existsByName(roleName)) {
                // 3. 역할이 존재하지 않으면 새로 생성
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Seeded role: {}", roleName);
            }
        }
    }
}


