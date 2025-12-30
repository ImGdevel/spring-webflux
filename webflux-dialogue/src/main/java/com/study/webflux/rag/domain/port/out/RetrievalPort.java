package com.study.webflux.rag.domain.port.out;

import com.study.webflux.rag.domain.model.memory.MemoryRetrievalResult;
import com.study.webflux.rag.domain.model.rag.RetrievalContext;

import reactor.core.publisher.Mono;

public interface RetrievalPort {
	Mono<RetrievalContext> retrieve(String query, int topK);

	Mono<MemoryRetrievalResult> retrieveMemories(String query, int topK);
}
