package com.connectsphere.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class JsonTestHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonTestHelper() {
    }

    static String readField(String json, String field) throws Exception {
        JsonNode node = OBJECT_MAPPER.readTree(json);
        return node.get(field).asText();
    }

    static void assertArrayContainsFieldValue(String json, String field, String expectedValue) throws Exception {
        JsonNode node = OBJECT_MAPPER.readTree(json);
        assertThat(node.isArray()).isTrue();
        boolean found = false;
        for (JsonNode item : node) {
            JsonNode fieldNode = item.get(field);
            if (fieldNode != null && expectedValue.equals(fieldNode.asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }
}
