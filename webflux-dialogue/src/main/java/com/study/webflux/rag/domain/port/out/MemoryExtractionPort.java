package com.study.webflux.rag.domain.port.out;

import com.study.webflux.rag.domain.model.memory.ExtractedMemory;
import com.study.webflux.rag.domain.model.memory.MemoryExtractionContext;

import reactor.core.publisher.Flux;

public interface MemoryExtractionPort {
	Flux<ExtractedMemory> extractMemories(MemoryExtractionContext context);
}
