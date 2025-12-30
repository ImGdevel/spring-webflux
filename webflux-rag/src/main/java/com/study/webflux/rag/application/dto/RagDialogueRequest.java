package com.study.webflux.rag.application.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RagDialogueRequest(
	@NotBlank String text,
	@NotNull Instant requestedAt
) {
}
