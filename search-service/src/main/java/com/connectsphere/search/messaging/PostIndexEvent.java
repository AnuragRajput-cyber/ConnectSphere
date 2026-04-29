package com.connectsphere.search.messaging;

public record PostIndexEvent(String postId, String content, String operation) {
}
