package com.study.webflux.rag.infrastructure.adapter.llm;

import com.study.webflux.rag.domain.model.llm.CompletionRequest;
import com.study.webflux.rag.domain.port.out.LlmPort;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ClaudeLlmAdapter implements LlmPort {

	@Override
	public Flux<String> streamCompletion(CompletionRequest request) {
		return Flux.error(new UnsupportedOperationException("Claude LLM adapter not yet implemented"));
	}

	@Override
	public Mono<String> complete(CompletionRequest request) {
		return Mono.error(new UnsupportedOperationException("Claude LLM adapter not yet implemented"));
	}
}
