package com.example.authentication.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인증 관련 DTO(Data Transfer Object) 클래스들
 * 
 * 이 클래스는 클라이언트와 서버 간의 데이터 전송을 위한 객체들을 정의합니다.
 * DTO는 다음과 같은 장점을 제공합니다:
 * - API 계약의 명확성: 클라이언트가 어떤 데이터를 보내고 받을지 명확히 정의
 * - 데이터 검증: 입력 데이터의 유효성을 검사
 * - 보안: 내부 엔티티 구조를 숨기고 필요한 데이터만 노출
 * - 버전 관리: API 변경 시 DTO만 수정하면 됨
 * 
 * 주요 DTO들:
 * - RegisterRequest: 회원가입 요청 데이터
 * - LoginRequest: 로그인 요청 데이터
 * - AuthResponse: 인증 응답 데이터 (토큰 포함)
 */
@Getter
public class AuthDtos {
	
	/**
	 * 회원가입 요청 DTO
	 * 
	 * 클라이언트가 회원가입을 요청할 때 전송하는 데이터를 담는 클래스입니다.
	 * 
	 * Lombok 어노테이션 설명:
	 * - @Getter: 모든 필드에 대한 getter 메서드 자동 생성
	 * - @Builder: 빌더 패턴 자동 생성 (객체 생성 시 체이닝 방식 사용 가능)
	 * - @NoArgsConstructor: 매개변수 없는 생성자 생성
	 * - @AllArgsConstructor: 모든 필드를 매개변수로 받는 생성자 생성
	 */
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RegisterRequest {
		
		/**
		 * 사용자 이메일 주소
		 * 
		 * @Email: 이메일 형식 검증 (예: user@example.com)
		 * @NotBlank: null, 빈 문자열, 공백만 있는 문자열을 허용하지 않음
		 */
		@Email
		@NotBlank
		private String email;
		
		/**
		 * 사용자 비밀번호
		 * 
		 * @NotBlank: null, 빈 문자열, 공백만 있는 문자열을 허용하지 않음
		 * 
		 * 보안 참고사항:
		 * - 실제 비밀번호는 서버에서 해시화되어 저장됩니다.
		 * - 클라이언트에서는 평문으로 전송되지만 HTTPS를 통해 암호화됩니다.
		 */
		@NotBlank
		private String password;
		
		/**
		 * 사용자 표시 이름
		 * 
		 * 실제 이름이나 닉네임 등, 사용자를 식별할 수 있는 표시용 이름입니다.
		 * 선택적 필드이므로 검증 어노테이션이 없습니다.
		 */
		private String displayName;
	}

	/**
	 * 로그인 요청 DTO
	 * 
	 * 클라이언트가 로그인을 요청할 때 전송하는 데이터를 담는 클래스입니다.
	 */
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LoginRequest {
		
		/**
		 * 사용자 이메일 주소
		 * 
		 * @Email: 이메일 형식 검증 (예: user@example.com)
		 * @NotBlank: null, 빈 문자열, 공백만 있는 문자열을 허용하지 않음
		 */
		@Email
		@NotBlank
		private String email;
		
		/**
		 * 사용자 비밀번호
		 * 
		 * @NotBlank: null, 빈 문자열, 공백만 있는 문자열을 허용하지 않음
		 * 
		 * 보안 참고사항:
		 * - 실제 비밀번호는 서버에서 해시화된 값과 비교됩니다.
		 * - 클라이언트에서는 평문으로 전송되지만 HTTPS를 통해 암호화됩니다.
		 */
		@NotBlank
		private String password;
	}

	/**
	 * 인증 응답 DTO
	 * 
	 * 로그인이나 회원가입 성공 시 클라이언트에게 반환하는 데이터를 담는 클래스입니다.
	 * JWT 토큰들을 포함하여 클라이언트가 인증된 상태를 유지할 수 있도록 합니다.
	 */
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AuthResponse {
		
		/**
		 * 사용자 고유 식별자
		 * 
		 * 데이터베이스에서 사용자를 식별하는 고유한 ID입니다.
		 * 클라이언트에서 사용자 정보를 요청할 때 사용됩니다.
		 */
		private Long userId;
		
		/**
		 * 사용자 이메일 주소
		 * 
		 * 로그인한 사용자의 이메일 주소입니다.
		 * 클라이언트에서 사용자 정보를 표시하는 데 사용됩니다.
		 */
		private String email;
		
		/**
		 * 액세스 토큰 (Access Token)
		 * 
		 * API 요청 시 인증을 위해 사용되는 JWT 토큰입니다.
		 * 짧은 수명을 가지며 (예: 15분), 만료되면 갱신 토큰으로 새로 발급받아야 합니다.
		 * 
		 * 사용 방법:
		 * - HTTP 헤더에 "Authorization: Bearer {accessToken}" 형태로 포함
		 * - 모든 보호된 API 요청에 필요
		 */
		private String accessToken;
		
		/**
		 * 갱신 토큰 (Refresh Token)
		 * 
		 * 액세스 토큰이 만료되었을 때 새로운 액세스 토큰을 발급받기 위해 사용되는 JWT 토큰입니다.
		 * 긴 수명을 가지며 (예: 7일), 보안을 위해 서버에 안전하게 저장됩니다.
		 * 
		 * 사용 방법:
		 * - 액세스 토큰 갱신 API에 전송
		 * - 로그아웃 시 서버에서 무효화
		 */
		private String refreshToken;
	}
}
