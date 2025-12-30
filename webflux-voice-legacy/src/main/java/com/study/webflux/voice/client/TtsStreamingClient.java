package com.study.webflux.voice.client;

import reactor.core.publisher.Flux;

public interface TtsStreamingClient {

	Flux<byte[]> streamAudio(String sentence);
}
