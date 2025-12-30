package com.study.webflux.rag.infrastructure.adapter.memory;

import com.study.webflux.rag.domain.model.memory.ExtractedMemory;
import com.study.webflux.rag.domain.model.memory.MemoryType;

record MemoryExtractionDto(
	String type,
	String content,
	float importance,
	String reasoning
) {
	ExtractedMemory toExtractedMemory() {
		return new ExtractedMemory(
			MemoryType.valueOf(type.toUpperCase()),
			content,
			importance,
			reasoning
		);
	}
}
