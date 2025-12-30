package com.study.webflux.rag.domain.model.memory;

public record ExtractedMemory(
	MemoryType type,
	String content,
	float importance,
	String reasoning
) {
	public ExtractedMemory {
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("content cannot be null or blank");
		}
		if (importance < 0.0f || importance > 1.0f) {
			throw new IllegalArgumentException("importance must be between 0.0 and 1.0");
		}
	}

	public Memory toMemory() {
		return Memory.create(type, content, importance);
	}
}
