package com.study.webflux.voice.v2;

import reactor.core.publisher.Flux;

/**
 * 외부 LLM 스트리밍 API 를 추상화한 클라이언트 인터페이스.
 *
 * <p>
 * 실제 구현에서는 WebClient 를 사용해 "스트리밍 응답"을 받는 형태가 될 수 있다.
 * </p>
 */
public interface LlmStreamingClient {

	/**
	 * 사용자의 프롬프트를 LLM 으로 전송하고, 토큰/조각 단위 스트림을 반환한다.
	 *
	 * @param prompt 사용자 입력 텍스트
	 * @return LLM 이 순차적으로 내보내는 텍스트 조각 스트림
	 */
	Flux<String> streamCompletion(String prompt);
}

