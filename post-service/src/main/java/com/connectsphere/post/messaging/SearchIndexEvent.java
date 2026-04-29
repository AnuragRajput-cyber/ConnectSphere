package com.connectsphere.post.messaging;

public record SearchIndexEvent(String postId, String content, String operation) {
}
