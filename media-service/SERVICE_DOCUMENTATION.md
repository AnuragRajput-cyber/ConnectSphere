# Media Service - SERVICE_DOCUMENTATION.md

## 1. Service Overview

**Service folder:** `media-service`  
**Type:** Spring Boot service  
**Port:** 8087  
**Run command:** `mvn -pl media-service spring-boot:run`

Handles media uploads and 24-hour stories, with local, AWS S3, or Azure Blob storage providers.

**Business responsibility:** Store files, return public URLs, create/view/delete stories, expire old stories.

**Data owned:** media, stories.

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
| media-service/Dockerfile | Container build recipe used by Docker Compose and Jenkins deployment. | Container build recipe used by Docker Compose and Jenkins deployment. | Related module build/runtime | Explain multi-stage/image build and why containerized services are portable. |
| media-service/README.md | Markdown documentation/readme file. | Markdown documentation/readme file. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/pom.xml | Maven project descriptor: declares Spring Boot parent, dependencies, build plugin, and Java version. | Maven project descriptor: declares Spring Boot parent, dependencies, build plugin, and Java version. | Maven, Spring Boot, Jenkins, Docker build | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/java/com/connectsphere/media/MediaServiceApplication.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/java/com/connectsphere/media/config/MediaStorageProperties.java | Configuration class: declares beans, security, OpenAPI, messaging, storage, or websocket setup. | Configuration class: declares beans, security, OpenAPI, messaging, storage, or websocket setup. | Spring application context and runtime configuration | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/java/com/connectsphere/media/config/OpenApiConfig.java | Configuration class: declares beans, security, OpenAPI, messaging, storage, or websocket setup. | Configuration class: declares beans, security, OpenAPI, messaging, storage, or websocket setup. | Spring application context and runtime configuration | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/java/com/connectsphere/media/controller/GlobalExceptionHandler.java | REST controller: exposes HTTP API routes and delegates business work to service classes. | REST controller: exposes HTTP API routes and delegates business work to service classes. | Service layer, DTOs, API gateway, frontend calls | Explain this as the HTTP boundary: validation happens here, but business rules stay in services. |
| media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | REST controller: exposes HTTP API routes and delegates business work to service classes. | REST controller: exposes HTTP API routes and delegates business work to service classes. | Service layer, DTOs, API gateway, frontend calls | Explain this as the HTTP boundary: validation happens here, but business rules stay in services. |
| media-service/src/main/java/com/connectsphere/media/dto/ApiMessageResponse.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| media-service/src/main/java/com/connectsphere/media/dto/MediaResponse.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| media-service/src/main/java/com/connectsphere/media/dto/StoryResponse.java | DTO/request/response record: defines API input/output contract and validation. | DTO/request/response record: defines API input/output contract and validation. | Controllers, services, API clients | Explain DTOs as API contracts that keep entity models separate from request/response payloads. |
| media-service/src/main/java/com/connectsphere/media/entity/Media.java | JPA entity/model: maps Java fields to a database table. | JPA entity/model: maps Java fields to a database table. | Repositories and database schema | Explain table ownership, UUID identifiers, and denormalized counters/fields where present. |
| media-service/src/main/java/com/connectsphere/media/entity/MediaType.java | JPA entity/model: maps Java fields to a database table. | JPA entity/model: maps Java fields to a database table. | Repositories and database schema | Explain table ownership, UUID identifiers, and denormalized counters/fields where present. |
| media-service/src/main/java/com/connectsphere/media/entity/Story.java | JPA entity/model: maps Java fields to a database table. | JPA entity/model: maps Java fields to a database table. | Repositories and database schema | Explain table ownership, UUID identifiers, and denormalized counters/fields where present. |
| media-service/src/main/java/com/connectsphere/media/exception/BadRequestException.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/java/com/connectsphere/media/exception/NotFoundException.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/java/com/connectsphere/media/repository/MediaRepository.java | Repository/DAO: Spring Data or Angular data access boundary for persistence/search. | Repository/DAO: Spring Data or Angular data access boundary for persistence/search. | Service layer and JPA/Elasticsearch persistence | Explain Spring Data derived queries and why services do not write SQL directly. |
| media-service/src/main/java/com/connectsphere/media/repository/StoryRepository.java | Repository/DAO: Spring Data or Angular data access boundary for persistence/search. | Repository/DAO: Spring Data or Angular data access boundary for persistence/search. | Service layer and JPA/Elasticsearch persistence | Explain Spring Data derived queries and why services do not write SQL directly. |
| media-service/src/main/java/com/connectsphere/media/service/MediaService.java | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Java source file: Spring Boot application, controller, service, entity, repository, DTO, config, or test. | Controllers, repositories, entities, messaging/storage clients | Explain this as the business logic layer and the best place to discuss transactions and edge cases. |
| media-service/src/main/java/com/connectsphere/media/service/MediaServiceImpl.java | Business service implementation: contains main domain logic and transaction boundaries. | Business service implementation: contains main domain logic and transaction boundaries. | Controllers, repositories, entities, messaging/storage clients | Explain this as the business logic layer and the best place to discuss transactions and edge cases. |
| media-service/src/main/java/com/connectsphere/media/storage/AzureBlobMediaStorageService.java | Storage adapter: stores uploaded media in local disk, AWS S3, or Azure Blob. | Storage adapter: stores uploaded media in local disk, AWS S3, or Azure Blob. | MediaServiceImpl and external storage provider | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/java/com/connectsphere/media/storage/LocalMediaStorageService.java | Storage adapter: stores uploaded media in local disk, AWS S3, or Azure Blob. | Storage adapter: stores uploaded media in local disk, AWS S3, or Azure Blob. | MediaServiceImpl and external storage provider | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/java/com/connectsphere/media/storage/MediaStorageService.java | Storage adapter: stores uploaded media in local disk, AWS S3, or Azure Blob. | Storage adapter: stores uploaded media in local disk, AWS S3, or Azure Blob. | MediaServiceImpl and external storage provider | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/java/com/connectsphere/media/storage/S3MediaStorageService.java | Storage adapter: stores uploaded media in local disk, AWS S3, or Azure Blob. | Storage adapter: stores uploaded media in local disk, AWS S3, or Azure Blob. | MediaServiceImpl and external storage provider | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/java/com/connectsphere/media/storage/StoredMediaAsset.java | Storage adapter: stores uploaded media in local disk, AWS S3, or Azure Blob. | Storage adapter: stores uploaded media in local disk, AWS S3, or Azure Blob. | MediaServiceImpl and external storage provider | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/resources/application-mysql.yml | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |
| media-service/src/main/resources/application.yml | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Spring configuration file for port, datasource, Eureka, cache, broker, storage, and actuator settings. | Related module build/runtime | Explain port, service name, Eureka registration, and environment-variable overrides. |
| media-service/src/test/java/com/connectsphere/media/MediaResourceIntegrationTest.java | REST controller: exposes HTTP API routes and delegates business work to service classes. | REST controller: exposes HTTP API routes and delegates business work to service classes. | Related module build/runtime | Know why the file exists and what runtime/build behavior would break if removed. |

## 4. Dependencies Used in This Service

| Dependency | Group | Purpose | Project Usage | Interview Notes |
| --- | --- | --- | --- | --- |
| spring-cloud-dependencies ${spring-cloud.version} | org.springframework.cloud | Project dependency used by framework/build/runtime code. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-boot-starter-web | org.springframework.boot | Builds REST controllers on embedded Tomcat. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-boot-starter-validation | org.springframework.boot | Bean validation for DTO constraints such as @NotBlank and @Email. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-boot-starter-data-jpa | org.springframework.boot | JPA/Hibernate persistence against MySQL. | Declared in pom/package for this service. | Explain repository pattern and service-owned databases. |
| spring-boot-starter-actuator | org.springframework.boot | Health and operational endpoints. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| spring-cloud-starter-netflix-eureka-client | org.springframework.cloud | Registers service with Eureka and enables discovery. | Declared in pom/package for this service. | Know that it decouples service locations from hardcoded host/port values. |
| springdoc-openapi-starter-webmvc-ui ${springdoc.version} | org.springdoc | Swagger/OpenAPI docs for REST endpoints. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| s3 2.26.28 | software.amazon.awssdk | AWS S3 SDK client for media storage. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| azure-storage-blob 12.27.0 | com.azure | Azure Blob Storage SDK for media storage. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |
| mysql-connector-j | com.mysql | MySQL JDBC driver. | Declared in pom/package for this service. | Explain repository pattern and service-owned databases. |
| spring-boot-starter-test | org.springframework.boot | JUnit/Spring test support. | Declared in pom/package for this service. | Be ready to say what breaks if this dependency is removed. |

## 5. API Endpoints of This Service

| Method | URL | Code File | Purpose | Request | Response | Auth | Database Interaction | Error Cases |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | /api/v1/media/upload | media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | Upload media through MediaResource.java. | Multipart form-data for file endpoints; JSON for non-file writes. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes media, stories. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| GET | /api/v1/media/post/{postId} | media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | Get media by post through MediaResource.java. | Path/query parameters only unless noted by controller signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes media, stories. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| GET | /api/v1/media/{mediaId} | media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | Get media by id through MediaResource.java. | Path/query parameters only unless noted by controller signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes media, stories. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| DELETE | /api/v1/media/{mediaId} | media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | Delete media through MediaResource.java. | JSON body matching the request DTO named in the controller method signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes media, stories. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| POST | /api/v1/stories | media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | Create story through MediaResource.java. | Multipart form-data for file endpoints; JSON for non-file writes. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes media, stories. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| GET | /api/v1/stories/active | media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | Get active stories through MediaResource.java. | Path/query parameters only unless noted by controller signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes media, stories. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| GET | /api/v1/stories/{storyId} | media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | Get story by id through MediaResource.java. | Path/query parameters only unless noted by controller signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes media, stories. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| POST | /api/v1/stories/{storyId}/view | media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | View story through MediaResource.java. | Multipart form-data for file endpoints; JSON for non-file writes. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes media, stories. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| DELETE | /api/v1/stories/{storyId} | media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | Delete story through MediaResource.java. | Multipart form-data for file endpoints; JSON for non-file writes. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes media, stories. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| GET | /api/v1/stories/user/{authorId} | media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | Get stories by user through MediaResource.java. | Path/query parameters only unless noted by controller signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes media, stories. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |
| GET | /api/v1/media/files/{filename} | media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java | Serve file through MediaResource.java. | Path/query parameters only unless noted by controller signature. | Usually JSON/DTO response; see response DTO records below. | Normally called behind gateway with JWT; service itself mostly trusts forwarded user/context in this codebase. | Reads/writes media, stories. | Domain BadRequest/NotFound become 400/404 through GlobalExceptionHandler where present; validation failures return client errors. |

## 6. Database / Model Details

**Database/storage:** MySQL database connectsphere_media plus file/blob storage.

| Entity/Class | Table | File | Important Fields | Ownership Notes |
| --- | --- | --- | --- | --- |
| Media | media | media-service/src/main/java/com/connectsphere/media/entity/Media.java | mediaId:String, uploaderId:String, url:String, mediaType:MediaType, sizeKb:long, mimeType:String, linkedPostId:String, uploadedAt:Instant, deleted:boolean | Owned by this service database |
| Story | stories | media-service/src/main/java/com/connectsphere/media/entity/Story.java | storyId:String, authorId:String, mediaUrl:String, caption:String, mediaType:MediaType, viewsCount:long, expiresAt:Instant, createdAt:Instant, active:boolean | Owned by this service database |

**Important DTO/API contracts:**

| Record/DTO | File | Fields |
| --- | --- | --- |
| MediaStorageProperties | media-service/src/main/java/com/connectsphere/media/config/MediaStorageProperties.java | String provider, String localDirectory, S3Properties s3, AzureProperties azure |
| ApiMessageResponse | media-service/src/main/java/com/connectsphere/media/dto/ApiMessageResponse.java | String message |
| MediaResponse | media-service/src/main/java/com/connectsphere/media/dto/MediaResponse.java | String mediaId, String uploaderId, String url, String mediaType, long sizeKb, String mimeType, String linkedPostId, boolean deleted, Instant uploadedAt |
| StoryResponse | media-service/src/main/java/com/connectsphere/media/dto/StoryResponse.java | String storyId, String authorId, String mediaUrl, String caption, String mediaType, long viewsCount, Instant expiresAt, Instant createdAt, boolean active |
| StoredMediaAsset | media-service/src/main/java/com/connectsphere/media/storage/StoredMediaAsset.java | String storageKey, String publicUrl |

```mermaid
erDiagram
    MEDIA {
        string id
    }
    STORIES {
        string id
    }
```

## 7. Communication With Other Services

| Source | Target | Method | Purpose | Related Files |
| --- | --- | --- | --- | --- |
| media-service | Local/AWS S3/Azure Blob | Storage SDK/filesystem | Persist uploaded media and story files | storage package |
| frontend/gateway | media-service | REST multipart | Upload media and stories | MediaResource.java |

## 8. Code Flow Examples

**Important code excerpt:**

From `media-service/src/main/java/com/connectsphere/media/controller/MediaResource.java`:

```java
package com.connectsphere.media.controller;
@RestController
@RequestMapping({"/api/v1", ""})
@Tag(name = "Media Service", description = "Media upload and 24-hour story endpoints.")
public class MediaResource {
    private final MediaService mediaService;
    private final MediaStorageService mediaStorageService;
    public MediaResource(MediaService mediaService, MediaStorageService mediaStorageService) {
        this.mediaService = mediaService;
        this.mediaStorageService = mediaStorageService;
    }
    @PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload media")
    public ResponseEntity<MediaResponse> uploadMedia(
            @RequestParam String uploaderId,
            @RequestParam(required = false) String linkedPostId,
            @RequestParam MultipartFile file,
            @RequestHeader(value = "X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String actorRole
    ) throws IOException {
        if (!isAdmin(actorRole) && !uploaderId.trim().equalsIgnoreCase(actorId.trim())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(mediaService.uploadMedia(uploaderId, linkedPostId, file));
    }
    @GetMapping("/media/post/{postId}")
    public ResponseEntity<List<MediaResponse>> getMediaByPost(@PathVariable String postId) {
        return ResponseEntity.ok(mediaService.getMediaByPost(postId));
```

**Interview explanation:** Start by naming the controller/API entry point, then explain how it delegates to service logic, how repository/storage/messaging is used, and what response DTO goes back to the caller.

## 9. Running This Service

**Local:**

```powershell
mvn -pl media-service spring-boot:run
```

**Docker/production:** the root `docker-compose.prod.yml` builds this module from `media-service/Dockerfile` and injects environment variables. Required infrastructure depends on the service: MySQL for persistent services, Redis for cached services, RabbitMQ for async event services, Elasticsearch for search, and storage credentials for media.

**Important environment variables found in config:** `AWS_REGION`, `AWS_S3_BUCKET`, `AWS_S3_KEY_PREFIX`, `AWS_S3_PUBLIC_BASE_URL`, `AZURE_STORAGE_ACCOUNT`, `AZURE_STORAGE_CONNECTION_STRING`, `AZURE_STORAGE_CONTAINER`, `AZURE_STORAGE_KEY_PREFIX`, `AZURE_STORAGE_PUBLIC_BASE_URL`, `EUREKA_SERVER_URL`, `MEDIA_LOCAL_DIRECTORY`, `MEDIA_STORAGE_PROVIDER`, `MYSQL_DB`, `MYSQL_HOST`, `MYSQL_PASSWORD`, `MYSQL_PORT`, `MYSQL_USERNAME`

**Common errors and fixes:**

| Error | Likely Cause | Fix |
| --- | --- | --- |
| Cannot connect to MySQL | Database container/service not running or wrong env vars | Start MySQL and check `MYSQL_HOST`, `MYSQL_DB`, username/password. |
| Eureka registration warning | Service registry not running | Start `service-registry` first or ignore during isolated tests. |
| Rabbit/Redis/Elasticsearch health down | Optional infrastructure not running | Start required container or disable health flags for local tests. |
| 401/403 | Missing or invalid JWT / role | Login again and send `Authorization: Bearer <token>`. |

## 10. Interview Notes for This Service

**Short answer:** Media Service handles media uploads and 24-hour stories, with local, aws s3, or azure blob storage providers.

**Deep answer:** Discuss why the service owns media, stories, how it exposes route contracts, and how it integrates with Called by frontend/gateway; may write to local disk, AWS S3, or Azure Blob.

**Common questions and best answers:**

| Question | Best Answer |
| --- | --- |
| Why is this a separate service? | Because store files, return public urls, create/view/delete stories, expire old stories. can evolve, scale, and fail independently from other platform domains. |
| What would break if it is down? | Features depending on Media Service fail, but unrelated services can continue if they do not require it synchronously. |
| How do you debug it? | Check container logs, `/actuator/health`, DB connectivity, Eureka registration, and the controller/service/repository flow. |
| How would you improve it? | Add stronger contract tests, distributed tracing, idempotency for writes, and production-grade metrics/alerts. |
