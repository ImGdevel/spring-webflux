package com.study.webflux.rag.infrastructure.adapter.vectordb.dto;

import java.util.List;

public record QdrantSearchRequest(
	List<Float> vector,
	int limit,
	boolean withPayload,
	QdrantFilter filter
) {
}
