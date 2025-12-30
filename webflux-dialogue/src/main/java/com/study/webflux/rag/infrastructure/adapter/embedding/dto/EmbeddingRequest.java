package com.study.webflux.rag.infrastructure.adapter.embedding.dto;

public record EmbeddingRequest(
	String input,
	String model
) {
}
