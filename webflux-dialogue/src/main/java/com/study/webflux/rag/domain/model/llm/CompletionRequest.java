package com.study.webflux.rag.domain.model.llm;

import java.util.List;
import java.util.Map;

public record CompletionRequest(
	List<Message> messages,
	String model,
	boolean stream,
	Map<String, Object> additionalParams
) {
	public CompletionRequest {
		if (messages == null || messages.isEmpty()) {
			throw new IllegalArgumentException("messages cannot be null or empty");
		}
		if (model == null || model.isBlank()) {
			throw new IllegalArgumentException("model cannot be null or blank");
		}
		if (additionalParams == null) {
			additionalParams = Map.of();
		}
	}

	public static CompletionRequest fromPrompt(String prompt, String model) {
		return new CompletionRequest(
			List.of(Message.user(prompt)),
			model,
			false,
			Map.of()
		);
	}

	public static CompletionRequest streaming(String prompt, String model) {
		return new CompletionRequest(
			List.of(Message.user(prompt)),
			model,
			true,
			Map.of()
		);
	}

	public static CompletionRequest withMessages(List<Message> messages, String model, boolean stream) {
		return new CompletionRequest(messages, model, stream, Map.of());
	}
}
