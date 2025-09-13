# Authentication Service

Spring Boot 기반 사용자 인증/인가 서비스 (JWT, 이메일 인증, RBAC, 소셜 로그인, 잠금, 활동 로그).

## 빠른 시작

1) JDK 21 필요 (Gradle Toolchains 자동 설치)
2) 빌드
```
./gradlew build -x test
```
3) 실행 (개발용 H2)
```
./gradlew bootRun
```

## 주요 기능
- 기본 로그인/회원가입 (비밀번호 bcrypt 해싱)
- 이메일 인증 후 최종 가입 완료
- 로그인 실패 누적 시 일정 시간 잠금
- JWT Access/Refresh 발급, 재발급, 로그아웃/블랙리스트
- RefreshToken/블랙리스트 Redis 관리
- RBAC(USER/MANAGER/ADMIN) 기반 인가
- 활동 로깅(가입/로그인/재발급/로그아웃) 및 ADMIN 조회 API
- Google OAuth2 로그인 (동일 이메일 계정 자동 연결/생성)

## API 개요
- POST `/api/auth/register` { email, password, displayName }
- POST `/api/auth/login` { email, password }
- GET  `/api/auth/verify?token=...`
- POST `/api/auth/token/refresh?refreshToken=...`
- POST `/api/auth/logout?refreshToken=...`
- GET  `/api/me`
- GET  `/api/admin/users/{userId}/activities` (ROLE_ADMIN)
- OAuth2: GET `/oauth2/authorization/google` → 성공 후 `/oauth2/success?accessToken=...&refreshToken=...` 리다이렉트

## 설정
`src/main/resources/application.properties` 참고. 배포 환경에서는 아래 샘플을 복제해 값을 채우세요.
- `src/main/resources/application-example.properties`
- `.env.sample`

필수 키
- `app.jwt.access-secret` / `app.jwt.refresh-secret`
- `spring.data.redis.host`, `spring.data.redis.port`
- (메일) `spring.mail.*`
- (구글) `spring.security.oauth2.client.registration.google.*`

## 개발 메모
- H2 메모리 DB (JPA ddl-auto=update). 실제 환경에선 RDB를 설정하세요.
- 메일 전송은 SMTP 설정이 필요합니다. 개발 중에는 로깅/모의 전송으로 대체 가능.
- OAuth2 성공 시 서비스가 Access/Refresh 토큰을 발급하여 프론트로 리다이렉트합니다.
- 보안 민감 엔드포인트는 `@PreAuthorize`로 보호됩니다.

## 다음 작업(제안)
- 소셜 계정 연결 동의 화면(동일 이메일 계정 병합/연결)
- 재인증 훅(비밀번호 재입력/TOTP 2FA)
- 프로덕션용 설정 분리(yaml/profile), 비밀키 Secret Manager 연동
