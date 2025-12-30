package com.study.webflux.voice.client;

import reactor.core.publisher.Mono;

public interface TtsClient {

	Mono<byte[]> synthesize(String sentence);
}
