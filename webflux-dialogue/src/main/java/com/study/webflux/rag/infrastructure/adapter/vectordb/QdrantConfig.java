package com.study.webflux.rag.infrastructure.adapter.vectordb;

public record QdrantConfig(
	String url,
	String apiKey,
	int vectorDimension,
	String collectionName
) {
}
