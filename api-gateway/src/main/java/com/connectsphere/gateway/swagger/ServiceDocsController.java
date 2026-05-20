package com.connectsphere.gateway.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class ServiceDocsController {

    private static final Map<String, String> SERVICE_DOC_TARGETS = Map.of(
            "auth", "http://auth-service/v3/api-docs",
            "posts", "http://post-service/v3/api-docs",
            "comments", "http://comment-service/v3/api-docs",
            "likes", "http://like-service/v3/api-docs",
            "follows", "http://follow-service/v3/api-docs",
            "notifications", "http://notification-service/v3/api-docs",
            "media", "http://media-service/v3/api-docs",
            "search", "http://search-service/v3/api-docs",
            "chat", "http://chat-service/v3/api-docs");

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public ServiceDocsController(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/service-docs/{service}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<JsonNode> getServiceDocs(@PathVariable String service) {
        String targetUrl = SERVICE_DOC_TARGETS.get(service);
        if (targetUrl == null) {
            return Mono.error(new IllegalArgumentException("Unknown service docs key: " + service));
        }

        return webClientBuilder.build()
                .get()
                .uri(targetUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::normalizeServers);
    }

    private JsonNode normalizeServers(JsonNode source) {
        ObjectNode copy = source.deepCopy();
        ArrayNode servers = objectMapper.createArrayNode();
        ObjectNode currentOrigin = objectMapper.createObjectNode();
        currentOrigin.put("url", "/");
        currentOrigin.put("description", "Current origin");
        servers.add(currentOrigin);
        copy.set("servers", servers);
        deduplicateVersionedPaths(copy);
        return copy;
    }

    private void deduplicateVersionedPaths(ObjectNode source) {
        JsonNode pathsNode = source.get("paths");
        if (!(pathsNode instanceof ObjectNode paths)) {
            return;
        }

        ObjectNode normalizedPaths = objectMapper.createObjectNode();
        Iterator<Map.Entry<String, JsonNode>> fields = paths.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String path = field.getKey();
            if (!path.startsWith("/api/v1/") && paths.has("/api/v1" + path)) {
                continue;
            }
            normalizedPaths.set(path, field.getValue());
        }

        source.set("paths", normalizedPaths);
    }
}
