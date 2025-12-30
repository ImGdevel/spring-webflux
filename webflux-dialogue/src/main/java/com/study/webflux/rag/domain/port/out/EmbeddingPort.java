package com.study.webflux.rag.domain.port.out;

import com.study.webflux.rag.domain.model.memory.MemoryEmbedding;

import reactor.core.publisher.Mono;

public interface EmbeddingPort {
	Mono<MemoryEmbedding> embed(String text);
}
