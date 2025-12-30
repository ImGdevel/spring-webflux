package com.study.webflux.rag.voice.client;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import reactor.core.publisher.Flux;

public class FakeTtsStreamingClient implements TtsStreamingClient {

	@Override
	public Flux<byte[]> streamAudio(String sentence) {
		String safe = sentence == null ? "" : sentence;

		return Flux.range(1, 5)
			.map(idx -> "AUDIO-CHUNK-" + idx + " (" + safe + ")")
			.map(chunk -> chunk.getBytes(StandardCharsets.UTF_8))
			.delayElements(Duration.ofMillis(50));
	}
}
