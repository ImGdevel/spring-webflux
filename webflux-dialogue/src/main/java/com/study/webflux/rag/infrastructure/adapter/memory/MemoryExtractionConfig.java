package com.study.webflux.rag.infrastructure.adapter.memory;

public record MemoryExtractionConfig(
	String model,
	int conversationThreshold,
	float importanceBoost,
	float importanceThreshold
) {
}
