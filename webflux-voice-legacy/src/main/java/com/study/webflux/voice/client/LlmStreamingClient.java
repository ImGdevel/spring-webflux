package com.study.webflux.voice.client;

import reactor.core.publisher.Flux;

public interface LlmStreamingClient {

	Flux<String> streamCompletion(String prompt);
}
