package com.study.webflux.voice.v1;

/**
 * LLM/TTS 데모에서 클라이언트가 전송하는 텍스트 요청 DTO.
 */
public record LlmRequest(
	String text
) {
}

