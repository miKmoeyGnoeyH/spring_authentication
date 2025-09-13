package com.example.authentication.security;

import com.example.authentication.domain.User;
import com.example.authentication.domain.SocialAccount;
import com.example.authentication.repository.RoleRepository;
import com.example.authentication.repository.UserRepository;
import com.example.authentication.repository.SocialAccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;

@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final SocialAccountRepository socialAccountRepository;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
		String email = (String) oAuth2User.getAttributes().getOrDefault("email", "");
		String name = (String) oAuth2User.getAttributes().getOrDefault("name", "");
		String provider = "google";
		String providerUserId = String.valueOf(oAuth2User.getAttributes().getOrDefault("sub", ""));
		if (email == null || email.isBlank()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "이메일 정보를 확인할 수 없습니다.");
			return;
		}
		User user = socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
				.map(SocialAccount::getUser)
				.orElseGet(() -> {
					var existing = userRepository.findByEmail(email).orElse(null);
					if (existing != null) {
						String consent = request.getParameter("consent");
						if (!"link".equalsIgnoreCase(consent)) {
							try {
								response.sendRedirect("/oauth2/consent?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8));
							} catch (IOException e) { /* ignore */ }
							return null;
						}
						SocialAccount sa = SocialAccount.builder()
								.user(existing)
								.provider(provider)
								.providerUserId(providerUserId)
								.email(email)
								.linkedAt(java.time.Instant.now())
								.build();
						socialAccountRepository.save(sa);
						return existing;
					}
					User created = User.builder()
							.email(email)
							.passwordHash("{noop}")
							.displayName(name)
							.emailVerified(true)
							.build();
					roleRepository.findByName("USER").ifPresent(r -> created.getRoles().add(r));
					User saved = userRepository.save(created);
					SocialAccount sa = SocialAccount.builder()
							.user(saved)
							.provider(provider)
							.providerUserId(providerUserId)
							.email(email)
							.linkedAt(java.time.Instant.now())
							.build();
					socialAccountRepository.save(sa);
					return saved;
				});
		if (user == null) {
			return; // consent redirect
		}

		var claims = new HashMap<String, Object>();
		claims.put("uid", user.getId());
		String access = jwtService.generateAccessToken(String.valueOf(user.getId()), claims);
		String refresh = jwtService.generateRefreshToken(String.valueOf(user.getId()), claims);
		refreshTokenService.store(String.valueOf(user.getId()), jwtService.parseRefreshClaims(refresh).getId(), refresh, Duration.ofSeconds(604800));

		String redirectUri = "/oauth2/success?accessToken=" + URLEncoder.encode(access, StandardCharsets.UTF_8) +
				"&refreshToken=" + URLEncoder.encode(refresh, StandardCharsets.UTF_8);
		response.sendRedirect(redirectUri);
	}
}
