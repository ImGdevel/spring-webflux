package com.study.webflux.rag.voice.client;

import java.time.Duration;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class FakeLlmStreamingClient implements LlmStreamingClient {

	@Override
	public Flux<String> streamCompletion(String prompt) {
		String base = prompt == null || prompt.isBlank() ? "안녕하세요, WebFlux 연습 중입니다." : prompt.trim();

		return Flux.just(
				"사용자 입력을 바탕으로 한 응답입니다. ",
				"지금은 RAG 음성 파이프라인을 연습하고 있습니다. ",
				"이 문장은 LLM 이 마지막으로 보내는 문장입니다."
			)
			.delayElements(Duration.ofMillis(300))
			.startWith("프롬프트: " + base + " ");
	}
}
