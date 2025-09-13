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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService, RefreshTokenService refreshTokenService, UserRepository userRepository, RoleRepository roleRepository, SocialAccountRepository socialAccountRepository, ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/**").permitAll()
						.requestMatchers("/api/auth/**", "/oauth2/**").permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(new JwtAuthenticationFilter(jwtService, refreshTokenService, userRepository), UsernamePasswordAuthenticationFilter.class)
				.httpBasic(Customizer.withDefaults());

		ClientRegistrationRepository clientRegistrationRepository = clientRegistrationRepositoryProvider.getIfAvailable();
		if (clientRegistrationRepository != null) {
			http.oauth2Login(oauth -> oauth
					.successHandler(new OAuth2LoginSuccessHandler(userRepository, roleRepository, socialAccountRepository, jwtService, refreshTokenService))
			);
		}
		return http.build();
	}
}


