package com.study.webflux.voice;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

/**
 * 실제 TTS 대신, 텍스트 토큰을 "오디오 청크" 라는 문자열로 변환하는 페이크 TTS 서비스.
 */
@Service
public class FakeTtsService {

	public Flux<String> synthesizeToken(String token) {
		String safe = token == null ? "" : token;
		return Flux.just(
				"AUDIO-CHUNK-START(" + safe + ")",
				"AUDIO-CHUNK-END(" + safe + ")"
			)
			.delayElements(Duration.ofMillis(200));
	}

	/**
	 * LLM이 완성한 전체 문장을 받아, 약 10글자 단위 청크마다 음성 데이터를 스트리밍하는 페이크 구현.
	 */
	public Flux<String> synthesizeFullText(String sentence) {
		String safe = sentence == null ? "" : sentence;
		if (safe.isEmpty()) {
			return synthesizeToken("");
		}

		List<String> chunks = splitInChunks(safe, 10);
		return Flux.fromIterable(chunks)
			.concatMap(this::synthesizeToken);
	}

	private List<String> splitInChunks(String text, int chunkSize) {
		List<String> chunks = new ArrayList<>();
		for (int index = 0; index < text.length(); index += chunkSize) {
			int end = Math.min(text.length(), index + chunkSize);
			chunks.add(text.substring(index, end));
		}
		return chunks;
	}
}
