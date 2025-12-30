package com.study.webflux.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
	@NotBlank
	String user,
	@NotBlank
	String message
) {
}
