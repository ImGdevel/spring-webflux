package com.study.webflux.rag.infrastructure.adapter.llm.dto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.study.webflux.rag.domain.model.llm.CompletionRequest;

public record OpenAiRequest(
	String model,
	List<Map<String, String>> messages,
	boolean stream
) {
	public static OpenAiRequest from(CompletionRequest request) {
		List<Map<String, String>> messages = request.messages().stream()
			.map(msg -> Map.of(
				"role", msg.role().getValue(),
				"content", msg.content()
			))
			.collect(Collectors.toList());

		return new OpenAiRequest(request.model(), messages, request.stream());
	}
}
