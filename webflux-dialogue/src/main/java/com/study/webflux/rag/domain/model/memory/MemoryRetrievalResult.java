package com.study.webflux.rag.domain.model.memory;

import java.util.ArrayList;
import java.util.List;

public record MemoryRetrievalResult(
	List<Memory> experientialMemories,
	List<Memory> factualMemories
) {
	public MemoryRetrievalResult {
		if (experientialMemories == null) {
			experientialMemories = List.of();
		}
		if (factualMemories == null) {
			factualMemories = List.of();
		}
	}

	public boolean isEmpty() {
		return experientialMemories.isEmpty() && factualMemories.isEmpty();
	}

	public int totalCount() {
		return experientialMemories.size() + factualMemories.size();
	}

	public List<Memory> allMemories() {
		List<Memory> all = new ArrayList<>();
		all.addAll(experientialMemories);
		all.addAll(factualMemories);
		return all;
	}

	public static MemoryRetrievalResult empty() {
		return new MemoryRetrievalResult(List.of(), List.of());
	}

	public static MemoryRetrievalResult of(List<Memory> experiential, List<Memory> factual) {
		return new MemoryRetrievalResult(experiential, factual);
	}
}
