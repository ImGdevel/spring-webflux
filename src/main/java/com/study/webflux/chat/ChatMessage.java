package com.study.webflux.chat;

import java.time.Instant;

public record ChatMessage(
	String user,
	String message,
	Instant createdAt
) {
}
