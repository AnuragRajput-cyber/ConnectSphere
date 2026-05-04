# Chat Service - SERVICE_DOCUMENTATION.md

## 1. Service Overview

**Service folder:** `chat-service`  
**Type:** Spring Boot service  
**Port:** 8089  
**Run command:** `mvn -pl chat-service spring-boot:run`

Owns direct message conversations and websocket delivery.

**Business responsibility:** Create conversations, send/read messages, clear conversation history, STOMP websocket chat and typing events.

**Data owned:** conversations, chat_messages.

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
| chat-service/Dockerfile | Container build recipe used by Docker Compose and Jenkins deployment. | Container build recipe used by Docker Compose and Jenkins deployment. | Related module build/runtime | Explain multi-stage/image build and why containerized services are portable. |
| chat-service/README.md | Markdown documentation/readme file. | Markdown documentation/readme file. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| chat-service/pom.xml | Maven project descriptor: declares Spring Boot parent, dependencies, build plugin, and Java version. | Maven project descriptor: declares Spring Boot parent, dependencies, build plugin, and Java version. | Maven, Spring Boot, Jenkins, Docker build | Know why the file exists and what runtime/build behavior would break if removed. |
| chat-service/src/main/java/com/connectsphere/chat/ChatServiceApplication.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| chat-service/src/main/java/com/connectsphere/chat/config/OpenApiConfig.java | Configuration class: declares beans, security, OpenAPI, messaging, storage, or websocket setup. | Configuration class: declares beans, security, OpenAPI, messaging, storage, or websocket setup. | Spring application context and runtime configuration | Know why the file exists and what runtime/build behavior would break if removed. |
| chat-service/src/main/java/com/connectsphere/chat/config/WebSocketConfig.java | Configuration class: declares beans, security, OpenAPI, messaging, storage, or websocket setup. | Configuration class: declares beans, security, OpenAPI, messaging, storage, or websocket setup. | Spring application context and runtime configuration | Know why the file exists and what runtime/build behavior would break if removed. |
| chat-service/src/main/java/com/connectsphere/chat/controller/ChatRestController.java | REST controller: exposes HTTP API routes and delegates business work to service classes. | REST controller: exposes HTTP API routes and delegates business work to service classes. | Service layer, DTOs, API gateway, frontend calls | Explain this as the HTTP boundary: validation happens here, but business rules stay in services. |
| chat-service/src/main/java/com/connectsphere/chat/controller/ChatSocketController.java | REST controller: exposes HTTP API routes and delegates business work to service classes. | REST controller: exposes HTTP API routes and delegates business work to service classes. | Service layer, DTOs, API gateway, frontend calls | Explain this as the HTTP boundary: validation happens here, but business rules stay in services. |
| chat-service/src/main/java/com/connectsphere/chat/dto/ChatMessageRequest.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| chat-service/src/main/java/com/connectsphere/chat/dto/ChatMessageResponse.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| chat-service/src/main/java/com/connectsphere/chat/dto/ConversationRequest.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| chat-service/src/main/java/com/connectsphere/chat/dto/ConversationResponse.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| chat-service/src/main/java/com/connectsphere/chat/dto/TypingIndicator.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| chat-service/src/main/java/com/connectsphere/chat/entity/ChatMessage.java | JPA entity/model: maps Java fields to a database table. | JPA entity/model: maps Java fields to a database table. | Repositories and database schema | Explain table ownership, UUID identifiers, and denormalized counters/fields where present. |
| chat-service/src/main/java/com/connectsphere/chat/entity/Conversation.java | JPA entity/model: maps Java fields to a database table. | JPA entity/model: maps Java fields to a database table. | Repositories and database schema | Explain table ownership, UUID identifiers, and denormalized counters/fields where present. |
| chat-service/src/main/java/com/connectsphere/chat/repository/ChatMessageRepository.java | Repository/DAO: Spring Data or Angular data access boundary for persistence/search. | Repository/DAO: Spring Data or Angular data access boundary for persistence/search. | Service layer and JPA/Elasticsearch persistence | Explain Spring Data derived queries and why services do not write SQL directly. |
| chat-service/src/main/java/com/connectsphere/chat/repository/ConversationRepository.java | Repository/DAO: Spring Data or Angular data access boundary for persistence/search. | Repository/DAO: Spring Data or Angular data access boundary for persistence/search. | Service layer and JPA/Elasticsearch persistence | Explain Spring Data derived queries and why services do not write SQL directly. |
| chat-service/src/main/java/com/connectsphere/chat/service/ChatService.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Controllers, repositories, entities, messaging/storage clients | Explain this as the business logic layer and the best place to discuss transactions and edge cases. |
| chat-service/src/main/java/com/connectsphere/chat/service/ChatServiceImpl.java | Business service implementation: contains main domain logic and transaction boundaries. | Business service implementation: contains main domain logic and transaction boundaries. | Controllers, repositories, entities, messaging/storage clients | Explain this as the business logic layer and the best place to discuss transactions and edge cases. |
| chat-service/src/main/resources/application-mysql.yml | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| chat-service/src/main/resources/application.yml | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Related module build/runtime | Explain port, service name, Eureka registration, and environment-variable overrides. |
| chat-service/src/test/java/com/connectsphere/chat/ChatRestIntegrationTest.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |

## 4. Dependencies Used in This Service

| Dependency | Group | Purpose | Project Usage | Interview Notes |
| --- | --- | --- | --- | --- |
| spring-cloud-dependencies ${spring-cloud.version} | org.springframework.cloud | Project dependency used by framework/build/runtime code. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-boot-starter-web | org.springframework.boot | Builds REST controllers on embedded Tomcat. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-boot-starter-websocket | org.springframework.boot | STOMP websocket messaging for chat. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-boot-starter-validation | org.springframework.boot | Bean validation for DTO constraints such as @NotBlank and @Email. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-boot-starter-data-jpa | org.springframework.boot | JPA/Hibernate persistence against MySQL. | Declared in pom/package for this service. | Explain repository pattern and service-owned databases. |
| spring-boot-starter-actuator | org.springframework.boot | Health and operational endpoints. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-cloud-starter-netflix-eureka-client | org.springframework.cloud | Registers service with Eureka and enables discovery. | Declared in pom/package for this service. | Know that it decouples service locations from hardcoded host/port values. |
| springdoc-openapi-starter-webmvc-ui ${springdoc.version} | org.springdoc | Swagger/OpenAPI docs for REST endpoints. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| mysql-connector-j | com.mysql | MySQL JDBC driver. | Declared in pom/package for this service. | Explain repository pattern and service-owned databases. |
| spring-boot-starter-test | org.springframework.boot | JUnit/Spring test support. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |

## 5. API Endpoints of This Service

| Method | URL | Code File | Purpose | Request | Response | Auth | Database Interaction | Error Cases |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | /api/v1/chat/conversations | chat-service/src/main/java/com/connectsphere/chat/controller/ChatRestController.java | Create conversation through ChatRestController.java. | JSON body matching the request DTO named in the controller method signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes conversations, chat_messages. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| GET | /api/v1/chat/conversations | chat-service/src/main/java/com/connectsphere/chat/controller/ChatRestController.java | Get conversations through ChatRestController.java. | Path/query parameters only unless noted by controller signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes conversations, chat_messages. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| POST | /api/v1/chat/messages | chat-service/src/main/java/com/connectsphere/chat/controller/ChatRestController.java | Save message through ChatRestController.java. | JSON body matching the request DTO named in the controller method signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes conversations, chat_messages. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| GET | /api/v1/chat/messages/{conversationId} | chat-service/src/main/java/com/connectsphere/chat/controller/ChatRestController.java | Get messages through ChatRestController.java. | Path/query parameters only unless noted by controller signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes conversations, chat_messages. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| DELETE | /api/v1/chat/messages/{conversationId} | chat-service/src/main/java/com/connectsphere/chat/controller/ChatRestController.java | Clear messages through ChatRestController.java. | JSON body matching the request DTO named in the controller method signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes conversations, chat_messages. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |

## 6. Database / Model Details

**Database/storage:** MySQL database connectsphere_chat.

| Entity/Class | Table | File | Important Fields | Ownership Notes |
| --- | --- | --- | --- | --- |
| ChatMessage | chat_messages | chat-service/src/main/java/com/connectsphere/chat/entity/ChatMessage.java | messageId:String, conversationId:String, senderId:String, recipientId:String, content:String, sentAt:Instant, read:boolean | Owned by this service database |
| Conversation | conversations | chat-service/src/main/java/com/connectsphere/chat/entity/Conversation.java | conversationId:String, participantOneId:String, participantTwoId:String, createdAt:Instant | Owned by this service database |

**Important DTO/API contracts:**

| Record/DTO | File | Fields |
| --- | --- | --- |
| ChatMessageRequest | chat-service/src/main/java/com/connectsphere/chat/dto/ChatMessageRequest.java | String conversationId, @NotBlank String senderId, @NotBlank String recipientId, @NotBlank String content |
| ChatMessageResponse | chat-service/src/main/java/com/connectsphere/chat/dto/ChatMessageResponse.java | String messageId, String conversationId, String senderId, String recipientId, String content, Instant sentAt, boolean read |
| ConversationRequest | chat-service/src/main/java/com/connectsphere/chat/dto/ConversationRequest.java | @NotBlank String participantOneId, @NotBlank String participantTwoId |
| ConversationResponse | chat-service/src/main/java/com/connectsphere/chat/dto/ConversationResponse.java | String conversationId, String participantOneId, String participantTwoId, Instant createdAt |
| TypingIndicator | chat-service/src/main/java/com/connectsphere/chat/dto/TypingIndicator.java | String conversationId, String senderId, String recipientId, boolean typing |

```mermaid
erDiagram
    CHAT_MESSAGES {
        string id
    }
    CONVERSATIONS {
        string id
    }
```

## 7. Communication With Other Services

| Source | Target | Method | Purpose | Related Files |
| --- | --- | --- | --- | --- |
| frontend/gateway | chat-service | REST + STOMP websocket | Direct messages, typing events, chat stream | ChatRestController.java, ChatSocketController.java |

## 8. Code Flow Examples

**Important code excerpt:**

From `chat-service/src/main/java/com/connectsphere/chat/controller/ChatRestController.java`:

```java
package com.connectsphere.chat.controller;
@RestController
@RequestMapping({"/api/v1/chat", "/chat"})
public class ChatRestController {
    private final ChatService chatService;
    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }
    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponse> createConversation(@Valid @RequestBody ConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createOrGetConversation(request));
    }
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(@RequestParam String userId) {
        return ResponseEntity.ok(chatService.getConversations(userId));
    }
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> saveMessage(@Valid @RequestBody ChatMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.saveMessage(request));
    }
    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable String conversationId) {
        return ResponseEntity.ok(chatService.getMessages(conversationId));
    }
    @DeleteMapping("/messages/{conversationId}")
    public ResponseEntity<Map<String, String>> clearMessages(@PathVariable String conversationId) {
        chatService.clearMessages(conversationId);
        return ResponseEntity.ok(Map.of("message", "Conversation messages cleared."));
```

**Interview explanation:** Start by naming the controller/API entry point, then explain how it delegates to service logic, how repository/storage/messaging is used, and what response DTO goes back to the caller.

## 9. Running This Service

**Local:**

```powershell
mvn -pl chat-service spring-boot:run
```

**Docker/production:** the root `docker-compose.prod.yml` builds this module from `chat-service/Dockerfile` and injects environment variables. Required infrastructure depends on the service: MySQL for persistent services, Redis for cached services, RabbitMQ for async event services, Elasticsearch for search, and storage credentials for media.

**Important environment variables found in config:** `EUREKA_SERVER_URL`, `MYSQL_DB`, `MYSQL_HOST`, `MYSQL_PASSWORD`, `MYSQL_PORT`, `MYSQL_USERNAME`

**Common errors and fixes:**

| Error | Likely Cause | Fix |
| --- | --- | --- |
| Cannot connect to MySQL | Database container/service not running or wrong env vars | Start MySQL and check `MYSQL_HOST`, `MYSQL_DB`, username/password. |
| Eureka registration warning | Service registry not running | Start `service-registry` first or ignore during isolated tests. |
| Rabbit/Redis/Elasticsearch health down | Optional infrastructure not running | Start required container or disable health flags for local tests. |
| 401/403 | Missing or invalid JWT / role | Login again and send `Authorization: Bearer <token>`. |

## 10. Interview Notes for This Service

**Short answer:** Chat Service owns direct message conversations and websocket delivery.

**Deep answer:** Discuss why the service owns conversations, chat_messages, how it exposes route contracts, and how it integrates with Called by frontend/gateway REST and websocket proxy.

**Common questions and best answers:**

| Question | Best Answer |
| --- | --- |
| Why is this a separate service? | Because create conversations, send/read messages, clear conversation history, stomp websocket chat and typing events. can evolve, scale, and fail independently from other platform domains. |
| What would break if it is down? | Features depending on Chat Service fail, but unrelated services can continue if they do not require it synchronously. |
| How do you debug it? | Check container logs, `/actuator/health`, DB connectivity, Eureka registration, and the controller/service/repository flow. |
| How would you improve it? | Add stronger contract tests, distributed tracing, idempotency for writes, and production-grade metrics/alerts. |
