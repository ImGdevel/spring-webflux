package com.study.webflux.voice;

import java.time.Duration;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 외부 LLM 호출을 흉내 내어 텍스트 토큰 스트림을 생성하는 페이크 서비스.
 */
@Service
public class FakeLlmService {

	public Flux<String> generate(String prompt) {
		String trimmed = prompt == null ? "" : prompt.trim();
		String base = trimmed.isEmpty() ? "안녕하세요" : trimmed;

		return Flux.just("LLM-START", "당신의 입력: " + base, "LLM-END")
			.delayElements(Duration.ofMillis(300));
	}

	/**
	 * 전체 응답을 하나의 문장으로 완성해 Mono로 전달.
	 */
	public Mono<String> composeResponse(String prompt) {
		return generate(prompt)
			.collectList()
			.map(tokens -> String.join(" ", tokens));
	}
}
