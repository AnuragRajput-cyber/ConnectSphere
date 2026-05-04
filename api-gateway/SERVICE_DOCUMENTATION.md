# API Gateway - SERVICE_DOCUMENTATION.md

## 1. Service Overview

**Service folder:** `api-gateway`  
**Type:** Spring Cloud Gateway service  
**Port:** 8080  
**Run command:** `mvn -pl api-gateway spring-boot:run`

Single backend entry point that routes browser API traffic to internal microservices and validates JWT tokens through auth-service.

**Business responsibility:** Routing, CORS, token validation, request identity headers, websocket proxying.

**Data owned:** No business data.

**Why this service exists:** In a microservices design, this responsibility changes independently from other domains. For interviews, explain that the service owns its data and exposes an API contract instead of letting other services touch its tables directly.

## 2. Service Architecture

```mermaid
graph TD
    Client[Frontend or API Gateway] --> Controller[Controller/API Layer]
    Controller --> Service[Service/Business Layer]
    Service --> Repository[Repository/Data Layer]
    Repository --> DB[(Service-owned storage)]
    Service --> External[Messaging/Other Services if configured]
```

**Internal structure:**

| Folder/File Area | Meaning |
| --- | --- |
| `src/main/java/.../controller` | HTTP boundary and exception mapping. |
| `src/main/java/.../service` | Business rules, transactions, orchestration. |
| `src/main/java/.../repository` | Spring Data persistence/search queries. |
| `src/main/java/.../entity` | Database table mapping. |
| `src/main/java/.../dto` | Request and response contracts. |
| `src/main/java/.../config` | Beans, OpenAPI, security, messaging, storage, or websocket setup. |
| `src/main/resources` | Runtime configuration with environment overrides. |
| `src/test` | Integration/unit tests proving important flows. |

**Request flow:** request enters controller, request DTO validation runs, service performs business checks, repository persists/queries data, response DTO returns to API Gateway/front-end.

## 3. File-by-File Explanation

| File Path | Purpose | What It Does | Connected With | Interview Notes |
| --- | --- | --- | --- | --- |
| api-gateway/Dockerfile | Container build recipe used by Docker Compose and Jenkins deployment. | Container build recipe used by Docker Compose and Jenkins deployment. | Related module build/runtime | Explain multi-stage/image build and why containerized services are portable. |
| api-gateway/README.md | Markdown documentation/readme file. | Markdown documentation/readme file. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| api-gateway/pom.xml | Maven project descriptor: declares Spring Boot parent, dependencies, build plugin, and Java version. | Maven project descriptor: declares Spring Boot parent, dependencies, build plugin, and Java version. | Maven, Spring Boot, Jenkins, Docker build | Know why the file exists and what runtime/build behavior would break if removed. |
| api-gateway/src/main/java/com/connectsphere/gateway/ApiGatewayApplication.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| api-gateway/src/main/java/com/connectsphere/gateway/security/AuthServiceClient.java | Security class: handles JWT, user details, filters, or route protection. | Security class: handles JWT, user details, filters, or route protection. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| api-gateway/src/main/java/com/connectsphere/gateway/security/GatewayAuthFilter.java | Security class: handles JWT, user details, filters, or route protection. | Security class: handles JWT, user details, filters, or route protection. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| api-gateway/src/main/resources/application.yml | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Related module build/runtime | Explain port, service name, Eureka registration, and environment-variable overrides. |
| api-gateway/src/test/java/com/connectsphere/gateway/ApiGatewayApplicationTests.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |

## 4. Dependencies Used in This Service

| Dependency | Group | Purpose | Project Usage | Interview Notes |
| --- | --- | --- | --- | --- |
| spring-cloud-dependencies ${spring-cloud.version} | org.springframework.cloud | Project dependency used by framework/build/runtime code. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-cloud-starter-gateway | org.springframework.cloud | Reactive API gateway and route filters. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-cloud-starter-netflix-eureka-client | org.springframework.cloud | Registers service with Eureka and enables discovery. | Declared in pom/package for this service. | Know that it decouples service locations from hardcoded host/port values. |
| spring-boot-starter-actuator | org.springframework.boot | Health and operational endpoints. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-boot-starter-test | org.springframework.boot | JUnit/Spring test support. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |

## 5. API Endpoints of This Service

This service does not expose business REST endpoints. It is infrastructure or frontend-only. For the frontend, see route documentation below.

## 6. Database / Model Details

**Database/storage:** None.

No JPA entity is owned by this service.

**Important DTO/API contracts:**

| Record/DTO | File | Fields |
| --- | --- | --- |
| TokenValidationRequest | api-gateway/src/main/java/com/connectsphere/gateway/security/AuthServiceClient.java | String token |

```mermaid
erDiagram
    SERVICE ||--|| CONFIG : uses
```

## 7. Communication With Other Services

| Source | Target | Method | Purpose | Related Files |
| --- | --- | --- | --- | --- |
| api-gateway | auth-service | REST WebClient | Validate JWT tokens and forward identity headers | GatewayAuthFilter.java, AuthServiceClient.java |
| api-gateway | all services | Spring Cloud Gateway + Eureka load-balancing | Route external API paths to internal services | application.yml routes |

## 8. Code Flow Examples

**Important code excerpt:**

**Interview explanation:** Start by naming the controller/API entry point, then explain how it delegates to service logic, how repository/storage/messaging is used, and what response DTO goes back to the caller.

## 9. Running This Service

**Local:**

```powershell
mvn -pl api-gateway spring-boot:run
```

**Docker/production:** the root `docker-compose.prod.yml` builds this module from `api-gateway/Dockerfile` and injects environment variables. Required infrastructure depends on the service: MySQL for persistent services, Redis for cached services, RabbitMQ for async event services, Elasticsearch for search, and storage credentials for media.

**Important environment variables found in config:** `AUTH_SERVICE_BASE_URL`, `EUREKA_SERVER_URL`

**Common errors and fixes:**

| Error | Likely Cause | Fix |
| --- | --- | --- |
| Cannot connect to MySQL | Database container/service not running or wrong env vars | Start MySQL and check `MYSQL_HOST`, `MYSQL_DB`, username/password. |
| Eureka registration warning | Service registry not running | Start `service-registry` first or ignore during isolated tests. |
| Rabbit/Redis/Elasticsearch health down | Optional infrastructure not running | Start required container or disable health flags for local tests. |
| 401/403 | Missing or invalid JWT / role | Login again and send `Authorization: Bearer <token>`. |

## 10. Interview Notes for This Service

**Short answer:** API Gateway single backend entry point that routes browser api traffic to internal microservices and validates jwt tokens through auth-service.

**Deep answer:** Discuss why the service owns No business data, how it exposes route contracts, and how it integrates with Calls auth-service /validate; routes to all downstream services through Eureka.

**Common questions and best answers:**

| Question | Best Answer |
| --- | --- |
| Why is this a separate service? | Because routing, cors, token validation, request identity headers, websocket proxying. can evolve, scale, and fail independently from other platform domains. |
| What would break if it is down? | Features depending on API Gateway fail, but unrelated services can continue if they do not require it synchronously. |
| How do you debug it? | Check container logs, `/actuator/health`, DB connectivity, Eureka registration, and the controller/service/repository flow. |
| How would you improve it? | Add stronger contract tests, distributed tracing, idempotency for writes, and production-grade metrics/alerts. |
