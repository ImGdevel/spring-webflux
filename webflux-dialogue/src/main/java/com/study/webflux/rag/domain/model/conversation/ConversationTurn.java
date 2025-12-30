package com.study.webflux.rag.domain.model.conversation;

import java.time.Instant;

public record ConversationTurn(
	String id,
	String query,
	String response,
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
		return new ConversationTurn(null, query, null, Instant.now());
	}

	public static ConversationTurn withId(String id, String query, String response, Instant createdAt) {
		return new ConversationTurn(id, query, response, createdAt);
	}

	public ConversationTurn withResponse(String response) {
		return new ConversationTurn(this.id, this.query, response, this.createdAt);
	}
}
