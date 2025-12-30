package com.study.webflux.rag.domain.port.out;

import com.study.webflux.rag.domain.model.llm.CompletionRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LlmPort {
	Flux<String> streamCompletion(CompletionRequest request);

	Mono<String> complete(CompletionRequest request);
}
