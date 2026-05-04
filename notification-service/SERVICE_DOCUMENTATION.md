# Notification Service - SERVICE_DOCUMENTATION.md

## 1. Service Overview

**Service folder:** `notification-service`  
**Type:** Spring Boot service  
**Port:** 8086  
**Run command:** `mvn -pl notification-service spring-boot:run`

Owns user notifications created directly or from RabbitMQ social events.

**Business responsibility:** Create single/bulk notifications, unread count, mark read, read-all, delete, cache recent notification lists.

**Data owned:** notifications.

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
| notification-service/Dockerfile | Container build recipe used by Docker Compose and Jenkins deployment. | Container build recipe used by Docker Compose and Jenkins deployment. | Related module build/runtime | Explain multi-stage/image build and why containerized services are portable. |
| notification-service/README.md | Markdown documentation/readme file. | Markdown documentation/readme file. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| notification-service/pom.xml | Maven project descriptor: declares Spring Boot parent, dependencies, build plugin, and Java version. | Maven project descriptor: declares Spring Boot parent, dependencies, build plugin, and Java version. | Maven, Spring Boot, Jenkins, Docker build | Know why the file exists and what runtime/build behavior would break if removed. |
| notification-service/src/main/java/com/connectsphere/notification/NotificationServiceApplication.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| notification-service/src/main/java/com/connectsphere/notification/config/OpenApiConfig.java | Configuration class: declares beans, security, OpenAPI, messaging, storage, or websocket setup. | Configuration class: declares beans, security, OpenAPI, messaging, storage, or websocket setup. | Spring application context and runtime configuration | Know why the file exists and what runtime/build behavior would break if removed. |
| notification-service/src/main/java/com/connectsphere/notification/controller/GlobalExceptionHandler.java | REST controller: exposes HTTP API routes and delegates business work to service classes. | REST controller: exposes HTTP API routes and delegates business work to service classes. | Service layer, DTOs, API gateway, frontend calls | Explain this as the HTTP boundary: validation happens here, but business rules stay in services. |
| notification-service/src/main/java/com/connectsphere/notification/controller/NotificationResource.java | REST controller: exposes HTTP API routes and delegates business work to service classes. | REST controller: exposes HTTP API routes and delegates business work to service classes. | Service layer, DTOs, API gateway, frontend calls | Explain this as the HTTP boundary: validation happens here, but business rules stay in services. |
| notification-service/src/main/java/com/connectsphere/notification/dto/ApiMessageResponse.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| notification-service/src/main/java/com/connectsphere/notification/dto/BulkNotificationRequest.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| notification-service/src/main/java/com/connectsphere/notification/dto/NotificationRequest.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| notification-service/src/main/java/com/connectsphere/notification/dto/NotificationResponse.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| notification-service/src/main/java/com/connectsphere/notification/entity/Notification.java | JPA entity/model: maps Java fields to a database table. | JPA entity/model: maps Java fields to a database table. | Repositories and database schema | Explain table ownership, UUID identifiers, and denormalized counters/fields where present. |
| notification-service/src/main/java/com/connectsphere/notification/entity/NotificationType.java | JPA entity/model: maps Java fields to a database table. | JPA entity/model: maps Java fields to a database table. | Repositories and database schema | Explain table ownership, UUID identifiers, and denormalized counters/fields where present. |
| notification-service/src/main/java/com/connectsphere/notification/exception/NotFoundException.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| notification-service/src/main/java/com/connectsphere/notification/messaging/NotificationEventListener.java | Messaging component: publishes or consumes RabbitMQ events. | Messaging component: publishes or consumes RabbitMQ events. | RabbitMQ exchange/queue bindings and async service flows | Know why the file exists and what runtime/build behavior would break if removed. |
| notification-service/src/main/java/com/connectsphere/notification/messaging/RabbitMessagingConfig.java | Messaging component: publishes or consumes RabbitMQ events. | Messaging component: publishes or consumes RabbitMQ events. | RabbitMQ exchange/queue bindings and async service flows | Know why the file exists and what runtime/build behavior would break if removed. |
| notification-service/src/main/java/com/connectsphere/notification/messaging/SocialNotificationEvent.java | Messaging component: publishes or consumes RabbitMQ events. | Messaging component: publishes or consumes RabbitMQ events. | RabbitMQ exchange/queue bindings and async service flows | Know why the file exists and what runtime/build behavior would break if removed. |
| notification-service/src/main/java/com/connectsphere/notification/repository/NotificationRepository.java | Repository/DAO: Spring Data or Angular data access boundary for persistence/search. | Repository/DAO: Spring Data or Angular data access boundary for persistence/search. | Service layer and JPA/Elasticsearch persistence | Explain Spring Data derived queries and why services do not write SQL directly. |
| notification-service/src/main/java/com/connectsphere/notification/service/NotificationService.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Controllers, repositories, entities, messaging/storage clients | Explain this as the business logic layer and the best place to discuss transactions and edge cases. |
| notification-service/src/main/java/com/connectsphere/notification/service/NotificationServiceImpl.java | Business service implementation: contains main domain logic and transaction boundaries. | Business service implementation: contains main domain logic and transaction boundaries. | Controllers, repositories, entities, messaging/storage clients | Explain this as the business logic layer and the best place to discuss transactions and edge cases. |
| notification-service/src/main/resources/application-mysql.yml | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| notification-service/src/main/resources/application.yml | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Related module build/runtime | Explain port, service name, Eureka registration, and environment-variable overrides. |
| notification-service/src/test/java/com/connectsphere/notification/NotificationResourceIntegrationTest.java | REST controller: exposes HTTP API routes and delegates business work to service classes. | REST controller: exposes HTTP API routes and delegates business work to service classes. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |

## 4. Dependencies Used in This Service

| Dependency | Group | Purpose | Project Usage | Interview Notes |
| --- | --- | --- | --- | --- |
| spring-cloud-dependencies ${spring-cloud.version} | org.springframework.cloud | Project dependency used by framework/build/runtime code. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-boot-starter-web | org.springframework.boot | Builds REST controllers on embedded Tomcat. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-boot-starter-validation | org.springframework.boot | Bean validation for DTO constraints such as @NotBlank and @Email. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-boot-starter-data-jpa | org.springframework.boot | JPA/Hibernate persistence against MySQL. | Declared in pom/package for this service. | Explain repository pattern and service-owned databases. |
| spring-boot-starter-cache | org.springframework.boot | Spring cache abstraction. | Declared in pom/package for this service. | Explain caching of read-heavy responses and invalidation risks. |
| spring-boot-starter-data-redis | org.springframework.boot | Redis cache integration. | Declared in pom/package for this service. | Explain caching of read-heavy responses and invalidation risks. |
| spring-boot-starter-amqp | org.springframework.boot | RabbitMQ publisher/listener support. | Declared in pom/package for this service. | Explain async event-driven notifications/search indexing. |
| spring-boot-starter-actuator | org.springframework.boot | Health and operational endpoints. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-cloud-starter-netflix-eureka-client | org.springframework.cloud | Registers service with Eureka and enables discovery. | Declared in pom/package for this service. | Know that it decouples service locations from hardcoded host/port values. |
| springdoc-openapi-starter-webmvc-ui ${springdoc.version} | org.springdoc | Swagger/OpenAPI docs for REST endpoints. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| mysql-connector-j | com.mysql | MySQL JDBC driver. | Declared in pom/package for this service. | Explain repository pattern and service-owned databases. |
| spring-boot-starter-test | org.springframework.boot | JUnit/Spring test support. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |

## 5. API Endpoints of This Service

| Method | URL | Code File | Purpose | Request | Response | Auth | Database Interaction | Error Cases |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | /api/v1/notifications | notification-service/src/main/java/com/connectsphere/notification/controller/NotificationResource.java | Create notification through NotificationResource.java. | JSON body matching the request DTO named in the controller method signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes notifications. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| POST | /api/v1/notifications/bulk | notification-service/src/main/java/com/connectsphere/notification/controller/NotificationResource.java | Send bulk through NotificationResource.java. | JSON body matching the request DTO named in the controller method signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes notifications. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| PATCH | /api/v1/notifications/{notificationId}/read | notification-service/src/main/java/com/connectsphere/notification/controller/NotificationResource.java | Mark as read through NotificationResource.java. | JSON body matching the request DTO named in the controller method signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes notifications. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| PATCH | /api/v1/notifications/read-all | notification-service/src/main/java/com/connectsphere/notification/controller/NotificationResource.java | Mark all read through NotificationResource.java. | JSON body matching the request DTO named in the controller method signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes notifications. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| GET | /api/v1/notifications/recipient/{recipientId} | notification-service/src/main/java/com/connectsphere/notification/controller/NotificationResource.java | Get by recipient through NotificationResource.java. | Path/query parameters only unless noted by controller signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes notifications. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| GET | /api/v1/notifications/recipient/{recipientId}/unread-count | notification-service/src/main/java/com/connectsphere/notification/controller/NotificationResource.java | Get unread count through NotificationResource.java. | Path/query parameters only unless noted by controller signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes notifications. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| DELETE | /api/v1/notifications/{notificationId} | notification-service/src/main/java/com/connectsphere/notification/controller/NotificationResource.java | Delete notification through NotificationResource.java. | JSON body matching the request DTO named in the controller method signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes notifications. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| GET | /api/v1/notifications | notification-service/src/main/java/com/connectsphere/notification/controller/NotificationResource.java | Get all through NotificationResource.java. | Path/query parameters only unless noted by controller signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes notifications. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |

## 6. Database / Model Details

**Database/storage:** MySQL database connectsphere_notification plus Redis cache.

| Entity/Class | Table | File | Important Fields | Ownership Notes |
| --- | --- | --- | --- | --- |
| Notification | notifications | notification-service/src/main/java/com/connectsphere/notification/entity/Notification.java | notificationId:String, recipientId:String, actorId:String, type:NotificationType, message:String, targetId:String, targetType:String, deepLinkUrl:String, read:boolean, createdAt:Instant | Owned by this service database |

**Important DTO/API contracts:**

| Record/DTO | File | Fields |
| --- | --- | --- |
| ApiMessageResponse | notification-service/src/main/java/com/connectsphere/notification/dto/ApiMessageResponse.java | String message |
| BulkNotificationRequest | notification-service/src/main/java/com/connectsphere/notification/dto/BulkNotificationRequest.java | @NotEmpty List<String> recipientIds, String actorId, @NotNull NotificationType type, @NotBlank String message, String targetId, String targetType, String deepLinkUrl |
| NotificationRequest | notification-service/src/main/java/com/connectsphere/notification/dto/NotificationRequest.java | @NotBlank String recipientId, String actorId, @NotNull NotificationType type, @NotBlank String message, String targetId, String targetType, String deepLinkUrl |
| NotificationResponse | notification-service/src/main/java/com/connectsphere/notification/dto/NotificationResponse.java | String notificationId, String recipientId, String actorId, String type, String message, String targetId, String targetType, String deepLinkUrl, boolean read, Instant createdAt ) implements Serializable { public static NotificationResponse from(Notification notification |
| SocialNotificationEvent | notification-service/src/main/java/com/connectsphere/notification/messaging/SocialNotificationEvent.java | String recipientId, String actorId, String type, String message, String targetId, String targetType, String deepLinkUrl |

```mermaid
erDiagram
    NOTIFICATIONS {
        string id
    }
```

## 7. Communication With Other Services

| Source | Target | Method | Purpose | Related Files |
| --- | --- | --- | --- | --- |
| RabbitMQ social.notification | notification-service | AMQP listener | Persist notifications emitted by social services | NotificationEventListener.java |
| frontend/gateway | notification-service | REST | Unread count and notification timeline | NotificationResource.java |

## 8. Code Flow Examples

**Important code excerpt:**

From `notification-service/src/main/java/com/connectsphere/notification/controller/NotificationResource.java`:

```java
package com.connectsphere.notification.controller;
@RestController
@RequestMapping({"/api/v1/notifications", "/notifications"})
@Tag(name = "Notification Service", description = "In-app and bulk notifications.")
public class NotificationResource {
    private final NotificationService notificationService;
    public NotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @PostMapping
    @Operation(summary = "Create a notification")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
        if (actorId != null && !actorId.isBlank() && !isAdmin(actorRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createNotification(request));
    }
    @PostMapping("/bulk")
    @Operation(summary = "Send bulk notifications")
    public ResponseEntity<List<NotificationResponse>> sendBulk(
            @Valid @RequestBody BulkNotificationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) {
```

**Interview explanation:** Start by naming the controller/API entry point, then explain how it delegates to service logic, how repository/storage/messaging is used, and what response DTO goes back to the caller.

## 9. Running This Service

**Local:**

```powershell
mvn -pl notification-service spring-boot:run
```

**Docker/production:** the root `docker-compose.prod.yml` builds this module from `notification-service/Dockerfile` and injects environment variables. Required infrastructure depends on the service: MySQL for persistent services, Redis for cached services, RabbitMQ for async event services, Elasticsearch for search, and storage credentials for media.

**Important environment variables found in config:** `APP_EVENTS_EXCHANGE`, `APP_NOTIFICATION_QUEUE`, `APP_NOTIFICATION_ROUTING_KEY`, `EUREKA_SERVER_URL`, `MYSQL_DB`, `MYSQL_HOST`, `MYSQL_PASSWORD`, `MYSQL_PORT`, `MYSQL_USERNAME`, `NOTIFICATION_CACHE_TTL`, `RABBITMQ_HOST`, `RABBITMQ_PASSWORD`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBIT_HEALTH_ENABLED`, `REDIS_HEALTH_ENABLED`, `REDIS_HOST`, `REDIS_PORT`, `SPRING_CACHE_TYPE`

**Common errors and fixes:**

| Error | Likely Cause | Fix |
| --- | --- | --- |
| Cannot connect to MySQL | Database container/service not running or wrong env vars | Start MySQL and check `MYSQL_HOST`, `MYSQL_DB`, username/password. |
| Eureka registration warning | Service registry not running | Start `service-registry` first or ignore during isolated tests. |
| Rabbit/Redis/Elasticsearch health down | Optional infrastructure not running | Start required container or disable health flags for local tests. |
| 401/403 | Missing or invalid JWT / role | Login again and send `Authorization: Bearer <token>`. |

## 10. Interview Notes for This Service

**Short answer:** Notification Service owns user notifications created directly or from rabbitmq social events.

**Deep answer:** Discuss why the service owns notifications, how it exposes route contracts, and how it integrates with Consumes social.notification events from RabbitMQ; called by frontend/gateway.

**Common questions and best answers:**

| Question | Best Answer |
| --- | --- |
| Why is this a separate service? | Because create single/bulk notifications, unread count, mark read, read-all, delete, cache recent notification lists. can evolve, scale, and fail independently from other platform domains. |
| What would break if it is down? | Features depending on Notification Service fail, but unrelated services can continue if they do not require it synchronously. |
| How do you debug it? | Check container logs, `/actuator/health`, DB connectivity, Eureka registration, and the controller/service/repository flow. |
| How would you improve it? | Add stronger contract tests, distributed tracing, idempotency for writes, and production-grade metrics/alerts. |
