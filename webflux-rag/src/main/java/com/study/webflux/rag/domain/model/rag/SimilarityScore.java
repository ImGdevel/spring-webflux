package com.study.webflux.rag.domain.model.rag;

public record SimilarityScore(int value) {
	public SimilarityScore {
		if (value < 0) {
			throw new IllegalArgumentException("Similarity score cannot be negative");
		}
	}

	public static SimilarityScore of(int value) {
		return new SimilarityScore(value);
	}

	public boolean isRelevant() {
		return value > 0;
	}
}
