# ConnectSphere `auth-service`

This service is aligned with the PDF sections for:

- `2.1` general auth and profile rules
- `2.2` guest access to login/registration and user search
- `2.3` registered-user registration and account management
- `2.4` admin account management support
- `4.1` Auth/User-Service entity, repository, service, and resource responsibilities
- `5` architecture naming (`com.connectsphere.auth`)
- `6` security requirements such as bcrypt and 24-hour JWT access tokens

## What is implemented

- User registration with email/password
- Login and logout
- 24-hour JWT access tokens plus rotating refresh tokens
- Token validation endpoint for downstream services
- Profile read and update
- Password change
- User search by username or full name
- Account deactivation
- MySQL-ready JPA model with an H2 default profile for local development
- OAuth2 client configuration placeholders for Google and GitHub

## API surface

The PDF names endpoints under `/auth/...`, while the non-functional requirements also call for versioned APIs under `/api/v1/...`.

To satisfy both, the controller exposes both route families:

- `/auth/register` and `/api/v1/auth/register`
- `/auth/login` and `/api/v1/auth/login`
- `/auth/logout` and `/api/v1/auth/logout`
- `/auth/refresh` and `/api/v1/auth/refresh`
- `/auth/profile` and `/api/v1/auth/profile`
- `/auth/password` and `/api/v1/auth/password`
- `/auth/search` and `/api/v1/auth/search`
- `/auth/deactivate` and `/api/v1/auth/deactivate`

## Swagger / OpenAPI

This service now includes Springdoc OpenAPI so you can inspect and test endpoints visually.

- Swagger UI: `/swagger-ui.html`
- Raw OpenAPI JSON: `/v3/api-docs`

The OpenAPI definition intentionally keeps server URLs relative instead of hard-coding localhost values.
That makes it easier to reuse the same docs later when the service is accessed through a single API gateway.

## Notes on OAuth

The PDF requires GitHub/Google OAuth support, but it does not define the website callback flow or provider credentials.

This starter includes:

- Spring Security OAuth2 client dependency
- an opt-in `oauth` profile in `application-oauth.yml`
- `AuthProvider` support in the domain model
- live Google and GitHub login flow through `/oauth2/authorization/google` and `/oauth2/authorization/github`
- JWT token response after successful provider login so downstream services can keep using the same auth model

The OAuth callback now completes inside `auth-service` itself and returns the same `AuthResponse` JSON structure as the normal email/password login endpoint.
That keeps the gateway integration simpler because both local login and social login end up producing the same token payload.

## Run locally

```bash
mvn spring-boot:run
```

To use MySQL instead of the in-memory H2 database:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

To enable GitHub/Google OAuth once credentials are available:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=oauth
```
