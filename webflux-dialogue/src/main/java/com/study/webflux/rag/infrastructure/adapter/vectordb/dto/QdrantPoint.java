package com.study.webflux.rag.infrastructure.adapter.vectordb.dto;

import java.util.List;
import java.util.Map;

public record QdrantPoint(
	String id,
	List<Float> vector,
	Map<String, Object> payload
) {
}
