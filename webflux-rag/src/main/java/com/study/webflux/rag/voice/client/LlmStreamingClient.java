package com.study.webflux.rag.voice.client;

import reactor.core.publisher.Flux;

public interface LlmStreamingClient {

	Flux<String> streamCompletion(String prompt);
}
