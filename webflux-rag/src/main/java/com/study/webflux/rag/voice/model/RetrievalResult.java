package com.study.webflux.rag.voice.model;

public record RetrievalResult(
	ConversationMessage message,
	int score
) {
}
