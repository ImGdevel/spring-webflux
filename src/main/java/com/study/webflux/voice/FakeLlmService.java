package com.study.webflux.voice;

import java.time.Duration;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 외부 LLM 호출을 흉내 내어 텍스트 토큰 스트림을 생성하는 페이크 서비스.
 *
 * <p>
 * - {@link #generate(String)}: 입력 프롬프트를 여러 단계 토큰으로 나눠 Flux 로 흘려보낸다. <br>
 * - {@link #composeResponse(String)}: 토큰들을 하나의 문장으로 모아 LLM 응답처럼 만든다.
 * </p>
 */
@Service
public class FakeLlmService {

	/**
	 * 간단한 문자열 토큰 스트림을 생성해서 LLM 토큰 응답처럼 흉내 낸다.
	 *
	 * @param prompt 사용자가 입력한 프롬프트
	 * @return "LLM-START", "당신의 입력: ...", "LLM-END" 토큰을 300ms 간격으로 발행하는 스트림
	 */
	public Flux<String> generate(String prompt) {
		String trimmed = prompt == null ? "" : prompt.trim();
		String base = trimmed.isEmpty() ? "안녕하세요" : trimmed;

		return Flux.just("LLM-START", "당신의 입력: " + base, "LLM-END")
			.delayElements(Duration.ofMillis(300));
	}

	/**
	 * 전체 응답을 하나의 문장으로 완성해 Mono로 전달.
	 *
	 * @param prompt 사용자가 입력한 프롬프트
	 * @return {@link #generate(String)} 로 만든 토큰들을 공백으로 이어 붙인 전체 문장
	 */
	public Mono<String> composeResponse(String prompt) {
		return generate(prompt)
			.collectList()
			.map(tokens -> String.join(" ", tokens));
	}
}
