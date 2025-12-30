package com.study.webflux.voice.v1;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

/**
 * 실제 TTS 대신, 텍스트 토큰을 "오디오 청크" 라는 문자열로 변환하는 페이크 TTS 서비스.
 *
 * <p>
 * - {@link #synthesizeToken(String)}: 단일 토큰을 시작/끝 표시가 있는 두 개의 "오디오 청크" 로 감싼다. <br>
 * - {@link #synthesizeFullText(String)}: 전체 문장을 고정 길이 청크로 나눈 뒤 순차적으로 합성한다.
 * </p>
 */
@Service
public class FakeTtsService {

	/**
	 * 하나의 텍스트 토큰을 받아 TTS 가 생성한 것처럼
	 * 시작/끝 두 개의 "오디오 청크" 문자열로 감싼다.
	 *
	 * @param token LLM 이 생성한 텍스트 토큰
	 * @return 200ms 간격으로 발행되는 시작/끝 청크 스트림
	 */
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
	 *
	 * @param sentence LLM 이 생성한 전체 문장
	 * @return 문장을 10글자씩 잘라 각 청크에 대해 {@link #synthesizeToken(String)} 을 순차 실행한 스트림
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
