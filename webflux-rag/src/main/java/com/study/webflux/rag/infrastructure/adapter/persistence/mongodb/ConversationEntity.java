package com.study.webflux.rag.infrastructure.adapter.persistence.mongodb;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "conversations")
public record ConversationEntity(
	@Id String id,
	String query,
	Instant createdAt
) {
}
