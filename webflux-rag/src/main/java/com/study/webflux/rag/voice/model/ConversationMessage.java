package com.study.webflux.rag.voice.model;

import java.time.Instant;

public record ConversationMessage(
	Long id,
	String query,
	Instant createdAt
) {
}
