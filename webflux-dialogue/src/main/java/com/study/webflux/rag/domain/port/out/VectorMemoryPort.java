package com.study.webflux.rag.domain.port.out;

import java.time.Instant;
import java.util.List;

import com.study.webflux.rag.domain.model.memory.Memory;
import com.study.webflux.rag.domain.model.memory.MemoryType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VectorMemoryPort {
	Mono<Memory> upsert(Memory memory, List<Float> embedding);

	Flux<Memory> search(List<Float> queryEmbedding, List<MemoryType> types, float importanceThreshold, int topK);

	Mono<Void> updateImportance(String memoryId, float newImportance, Instant lastAccessedAt, int accessCount);
}
