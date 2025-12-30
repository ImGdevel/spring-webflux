package com.study.webflux.rag.domain.model.llm;

public record Message(
	MessageRole role,
	String content
) {
	public Message {
		if (role == null) {
			throw new IllegalArgumentException("role cannot be null");
		}
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("content cannot be null or blank");
		}
	}

	public static Message user(String content) {
		return new Message(MessageRole.USER, content);
	}

	public static Message system(String content) {
		return new Message(MessageRole.SYSTEM, content);
	}

	public static Message assistant(String content) {
		return new Message(MessageRole.ASSISTANT, content);
	}
}
