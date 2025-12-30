package com.study.webflux.voice.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "conversations")
public record ConversationMessage(
	@Id String id,
	String query,
	Instant createdAt
) {
}
