package com.study.webflux.rag.infrastructure.adapter.vectordb.dto;

import java.util.Map;

public record QdrantScoredPoint(
	String id,
	float score,
	Map<String, Object> payload
) {
}
