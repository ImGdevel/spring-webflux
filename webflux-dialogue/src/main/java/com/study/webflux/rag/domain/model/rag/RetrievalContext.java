package com.study.webflux.rag.domain.model.rag;

import java.util.List;

public record RetrievalContext(
	String query,
	List<RetrievalDocument> documents
) {
	public RetrievalContext {
		if (query == null || query.isBlank()) {
			throw new IllegalArgumentException("query cannot be null or blank");
		}
		if (documents == null) {
			documents = List.of();
		}
	}

	public boolean isEmpty() {
		return documents.isEmpty();
	}

	public int documentCount() {
		return documents.size();
	}

	public static RetrievalContext empty(String query) {
		return new RetrievalContext(query, List.of());
	}

	public static RetrievalContext of(String query, List<RetrievalDocument> documents) {
		return new RetrievalContext(query, documents);
	}
}
