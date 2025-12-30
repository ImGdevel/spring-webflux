package com.study.webflux.voice.v2;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * v2 음성 파이프라인에서 클라이언트가 보내는 요청 DTO.
 *
 * <p>
 * - {@code text}: 사용자가 LLM 에 전달하고 싶은 원본 텍스트<br>
 * - {@code requestedAt}: 사용자가 요청을 생성한 시각(클라이언트 기준)
 * </p>
 */
public record VoiceV2Request(
	@NotBlank(message = "text는 비어 있을 수 없습니다.")
	String text,

	@NotNull(message = "requestedAt은 NULL일 수 없습니다.")
	Instant requestedAt
) {
}

