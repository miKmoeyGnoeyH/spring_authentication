package com.example.authentication.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class AuthDtos {
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RegisterRequest {
		@Email
		@NotBlank
		private String email;
		@NotBlank
		private String password;
		private String displayName;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LoginRequest {
		@Email
		@NotBlank
		private String email;
		@NotBlank
		private String password;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AuthResponse {
		private Long userId;
		private String email;
		private String accessToken;
		private String refreshToken;
	}
}
