package com.study.webflux.rag.infrastructure.adapter.llm;

public record OpenAiConfig(
	String apiKey,
	String baseUrl,
	String model
) {
}
