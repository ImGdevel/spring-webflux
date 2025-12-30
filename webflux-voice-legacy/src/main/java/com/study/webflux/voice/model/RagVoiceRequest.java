package com.study.webflux.voice.model;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RagVoiceRequest(
	@NotBlank String text,
	@NotNull Instant requestedAt
) {
}
