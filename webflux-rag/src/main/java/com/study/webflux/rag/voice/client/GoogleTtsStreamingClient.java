package com.study.webflux.rag.voice.client;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class GoogleTtsStreamingClient implements TtsStreamingClient {

	@Override
	public Flux<byte[]> streamAudio(String sentence) {
		return Flux.error(new UnsupportedOperationException("Google TTS integration not implemented yet"));
	}
}
