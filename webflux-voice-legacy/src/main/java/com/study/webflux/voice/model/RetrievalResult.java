package com.study.webflux.voice.model;

public record RetrievalResult(
	ConversationMessage message,
	int score
) {
}
