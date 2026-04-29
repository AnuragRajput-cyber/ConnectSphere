package com.connectsphere.search.document;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "connectsphere-posts")
public record PostSearchDocument(
        @Id String postId,
        @Field(type = FieldType.Text) String content,
        @Field(type = FieldType.Keyword) List<String> hashtags,
        @Field(type = FieldType.Date, format = DateFormat.date_time) Instant updatedAt
) {
}
