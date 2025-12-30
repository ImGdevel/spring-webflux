package com.study.webflux.rag.infrastructure.adapter.llm.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiStreamResponse(List<OpenAiChoice> choices) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record OpenAiChoice(OpenAiDelta delta) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record OpenAiDelta(String content) {
	}
}
