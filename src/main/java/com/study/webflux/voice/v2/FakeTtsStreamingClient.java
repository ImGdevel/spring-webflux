package com.study.webflux.voice.v2;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

/**
 * 실제 TTS 스트리밍 API 대신 사용하는 페이크 구현.
 *
 * <p>
 * 한 문장을 받아 "AUDIO-CHUNK" 문자열을 바이너리로 감싼 후
 * 여러 청크로 나누어 스트리밍하는 형태를 흉내 낸다.
 * </p>
 */
@Component
public class FakeTtsStreamingClient implements TtsStreamingClient {

	@Override
	public Flux<byte[]> streamAudio(String sentence) {
		String safe = sentence == null ? "" : sentence;

		return Flux.range(1, 5)
			.map(idx -> "AUDIO-CHUNK-" + idx + " (" + safe + ")")
			.map(chunk -> chunk.getBytes(StandardCharsets.UTF_8))
			.delayElements(Duration.ofMillis(200));
	}
}

