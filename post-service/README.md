# ConnectSphere `post-service`

This service implements the case study's post-management scope:

- post creation and retrieval
- author timelines
- visibility changes
- soft deletion
- feed lookup by followed-user ids
- content search
- post counters for likes, comments, and shares

## What is implemented

- `Post` entity with visibility, post type, media URLs, counters, and audit fields
- `PostRepository` with the case-study lookup methods
- `PostService` and `PostServiceImpl` for post workflows and counter operations
- `PostResource` exposing both `/posts/**` and `/api/v1/posts/**`
- H2 by default and MySQL profile support
- integration tests for create, update, search, feed, visibility, counters, and soft delete

## Swagger / OpenAPI

This service now includes Springdoc OpenAPI so you can inspect and test endpoints visually.

- Swagger UI: `/swagger-ui.html`
- Raw OpenAPI JSON: `/v3/api-docs`

The OpenAPI definition intentionally avoids fixed server URLs so the same docs can later be served through
your API gateway without needing a separate service-specific rewrite.

## Run locally

```bash
mvn spring-boot:run
```

To use MySQL:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
