package com.study.webflux.rag.infrastructure.adapter.embedding;

public record OpenAiEmbeddingConfig(
	String apiKey,
	String baseUrl,
	String model
) {
}
