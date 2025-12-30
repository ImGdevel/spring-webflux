package com.study.webflux.rag.infrastructure.adapter.vectordb.dto;

import java.util.List;

public record QdrantUpsertRequest(
	List<QdrantPoint> points
) {
}
