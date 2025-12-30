package com.study.webflux.rag.domain.model.memory;

import java.util.List;

public record MemoryEmbedding(
	String text,
	List<Float> vector
) {
	public MemoryEmbedding {
		if (text == null || text.isBlank()) {
			throw new IllegalArgumentException("text cannot be null or blank");
		}
		if (vector == null || vector.isEmpty()) {
			throw new IllegalArgumentException("vector cannot be null or empty");
		}
	}

	public static MemoryEmbedding of(String text, List<Float> vector) {
		return new MemoryEmbedding(text, List.copyOf(vector));
	}
}
