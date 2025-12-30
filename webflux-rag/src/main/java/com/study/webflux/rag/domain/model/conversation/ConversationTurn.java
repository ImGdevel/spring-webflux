package com.study.webflux.rag.domain.model.conversation;

import java.time.Instant;

public record ConversationTurn(
	String id,
	String query,
	Instant createdAt
) {
	public ConversationTurn {
		if (query == null || query.isBlank()) {
			throw new IllegalArgumentException("query cannot be null or blank");
		}
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public static ConversationTurn create(String query) {
		return new ConversationTurn(null, query, Instant.now());
	}

	public static ConversationTurn withId(String id, String query, Instant createdAt) {
		return new ConversationTurn(id, query, createdAt);
	}
}
