package com.study.webflux.rag.infrastructure.adapter.embedding.dto;

import java.util.List;

public record EmbeddingResponse(
	List<EmbeddingData> data
) {
	public record EmbeddingData(
		List<Float> embedding
	) {
	}
}
