package com.study.webflux.rag.application.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.study.webflux.rag.domain.model.memory.Memory;
import com.study.webflux.rag.domain.model.memory.MemoryRetrievalResult;
import com.study.webflux.rag.domain.model.memory.MemoryType;
import com.study.webflux.rag.domain.port.out.EmbeddingPort;
import com.study.webflux.rag.domain.port.out.VectorMemoryPort;
import com.study.webflux.rag.infrastructure.adapter.memory.MemoryExtractionConfig;

import reactor.core.publisher.Mono;

@Service
public class MemoryRetrievalService {

	private final EmbeddingPort embeddingPort;
	private final VectorMemoryPort vectorMemoryPort;
	private final float importanceBoost;
	private final float importanceThreshold;
	private final float recencyWeight = 0.1f;

	public MemoryRetrievalService(
		EmbeddingPort embeddingPort,
		VectorMemoryPort vectorMemoryPort,
		MemoryExtractionConfig config
	) {
		this.embeddingPort = embeddingPort;
		this.vectorMemoryPort = vectorMemoryPort;
		this.importanceBoost = config.importanceBoost();
		this.importanceThreshold = config.importanceThreshold();
	}

	public Mono<MemoryRetrievalResult> retrieveMemories(String query, int topK) {
		return embeddingPort.embed(query)
			.flatMap(embedding -> {
				List<MemoryType> types = List.of(MemoryType.EXPERIENTIAL, MemoryType.FACTUAL);

				return vectorMemoryPort.search(embedding.vector(), types, importanceThreshold, topK * 2)
					.collectList()
					.map(memories -> memories.stream()
						.sorted(Comparator.comparing(
							(Memory m) -> m.calculateRankedScore(recencyWeight)
						).reversed())
						.limit(topK)
						.toList()
					)
					.map(this::groupByType);
			})
			.flatMap(this::updateAccessMetrics);
	}

	private MemoryRetrievalResult groupByType(List<Memory> memories) {
		List<Memory> experiential = memories.stream()
			.filter(m -> m.type() == MemoryType.EXPERIENTIAL)
			.toList();

		List<Memory> factual = memories.stream()
			.filter(m -> m.type() == MemoryType.FACTUAL)
			.toList();

		return MemoryRetrievalResult.of(experiential, factual);
	}

	private Mono<MemoryRetrievalResult> updateAccessMetrics(MemoryRetrievalResult result) {
		return Mono.just(result.allMemories())
			.flatMapMany(memories -> reactor.core.publisher.Flux.fromIterable(memories))
			.flatMap(memory -> {
				Memory updated = memory.withAccess(importanceBoost);
				return vectorMemoryPort.updateImportance(
					updated.id(),
					updated.importance(),
					updated.lastAccessedAt(),
					updated.accessCount()
				).thenReturn(updated);
			})
			.collectList()
			.map(updatedMemories -> {
				List<Memory> experiential = updatedMemories.stream()
					.filter(m -> m.type() == MemoryType.EXPERIENTIAL)
					.toList();
				List<Memory> factual = updatedMemories.stream()
					.filter(m -> m.type() == MemoryType.FACTUAL)
					.toList();
				return MemoryRetrievalResult.of(experiential, factual);
			});
	}
}
