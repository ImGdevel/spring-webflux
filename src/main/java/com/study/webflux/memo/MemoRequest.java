package com.study.webflux.memo;

import jakarta.validation.constraints.NotBlank;

public record MemoRequest(
	@NotBlank(message = "내용은 비어 있을 수 없습니다.")
	String content
) {
}
