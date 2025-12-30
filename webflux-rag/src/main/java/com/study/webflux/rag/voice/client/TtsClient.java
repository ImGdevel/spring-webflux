package com.study.webflux.rag.voice.client;

import reactor.core.publisher.Mono;

public interface TtsClient {

	Mono<byte[]> synthesize(String sentence);
}
