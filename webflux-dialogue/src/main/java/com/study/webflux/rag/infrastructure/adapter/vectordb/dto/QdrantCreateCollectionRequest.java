package com.study.webflux.rag.infrastructure.adapter.vectordb.dto;

public record QdrantCreateCollectionRequest(
	VectorParams vectors
) {
	public record VectorParams(
		int size,
		String distance
	) {
	}
}
