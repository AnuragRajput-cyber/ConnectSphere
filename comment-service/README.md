# ConnectSphere `comment-service`

This service implements the case study's threaded discussion layer:

- top-level comments per post
- two-level replies
- comment edit and soft delete
- simple comment-like counters
- comment count lookup by post

## Swagger / OpenAPI

- Swagger UI: `/swagger-ui.html`
- Raw OpenAPI JSON: `/v3/api-docs`

## Run locally

```bash
mvn spring-boot:run
```

To use MySQL:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
