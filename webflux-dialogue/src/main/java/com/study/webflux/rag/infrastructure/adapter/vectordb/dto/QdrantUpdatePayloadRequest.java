package com.study.webflux.rag.infrastructure.adapter.vectordb.dto;

import java.util.List;
import java.util.Map;

public record QdrantUpdatePayloadRequest(
	List<String> points,
	Map<String, Object> payload
) {
}
