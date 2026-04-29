package com.connectsphere.search.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "connectsphere-hashtags")
public record HashtagSearchDocument(
        @Id String hashtagId,
        @Field(type = FieldType.Keyword) String tag,
        @Field(type = FieldType.Long) long postCount,
        @Field(type = FieldType.Date, format = DateFormat.date_time) Instant lastUsedAt
) {
}
