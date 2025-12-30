package com.study.webflux.memo;

import java.time.Instant;

public record Memo(
	Long id,
	String content,
	Instant createdAt
) {
}
