package com.example.authentication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정 클래스
 * 
 * 이 클래스는 JPA 관련 설정을 구성합니다.
 * 엔티티의 생성일시, 수정일시를 자동으로 관리하는 JPA Auditing을 활성화합니다.
 * 
 * 주요 기능:
 * - JPA Auditing 활성화 (@EnableJpaAuditing)
 * - 엔티티의 @CreatedDate, @LastModifiedDate 자동 설정
 * - AuditingEntityListener 자동 등록
 * 
 * @Configuration: 이 클래스를 Spring 설정 클래스로 등록합니다.
 * @EnableJpaAuditing: JPA Auditing 기능을 활성화합니다.
 * 
 * JPA Auditing이 활성화되면:
 * - @CreatedDate: 엔티티가 처음 저장될 때 현재 시간이 자동 설정됩니다.
 * - @LastModifiedDate: 엔티티가 수정될 때마다 현재 시간이 자동 업데이트됩니다.
 * - @CreatedBy, @LastModifiedBy: 생성자, 수정자 정보도 자동 설정 가능합니다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}


