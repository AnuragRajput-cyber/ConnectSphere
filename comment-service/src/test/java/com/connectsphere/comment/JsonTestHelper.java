package com.connectsphere.comment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonTestHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonTestHelper() {
    }

    public static String readField(String json, String field) throws Exception {
        JsonNode node = OBJECT_MAPPER.readTree(json);
        return node.get(field).asText();
    }
}
