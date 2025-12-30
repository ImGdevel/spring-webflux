package com.study.webflux.rag.infrastructure.adapter.retrieval;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.study.webflux.rag.application.service.MemoryRetrievalService;
import com.study.webflux.rag.domain.model.memory.MemoryRetrievalResult;
import com.study.webflux.rag.domain.model.rag.RetrievalContext;
import com.study.webflux.rag.domain.model.rag.RetrievalDocument;
import com.study.webflux.rag.domain.port.out.ConversationRepository;
import com.study.webflux.rag.domain.port.out.RetrievalPort;

import reactor.core.publisher.Mono;

@Component
@Primary
public class VectorMemoryRetrievalAdapter implements RetrievalPort {

	private final MemoryRetrievalService memoryRetrievalService;
	private final ConversationRepository conversationRepository;

	public VectorMemoryRetrievalAdapter(
		MemoryRetrievalService memoryRetrievalService,
		ConversationRepository conversationRepository
	) {
		this.memoryRetrievalService = memoryRetrievalService;
		this.conversationRepository = conversationRepository;
	}

	@Override
	public Mono<RetrievalContext> retrieve(String query, int topK) {
		Mono<RetrievalContext> conversationContext = conversationRepository.findAll()
			.collectList()
			.map(turns -> turns.stream()
				.map(turn -> {
					int score = calculateSimilarity(query, turn.query());
					return RetrievalDocument.of(turn.query(), score);
				})
				.filter(doc -> doc.score().isRelevant())
				.sorted((a, b) -> Integer.compare(b.score().value(), a.score().value()))
				.limit(topK)
				.toList()
			)
			.map(docs -> RetrievalContext.of(query, docs));

		return conversationContext;
	}

	@Override
	public Mono<MemoryRetrievalResult> retrieveMemories(String query, int topK) {
		return memoryRetrievalService.retrieveMemories(query, topK)
			.onErrorResume(error -> {
				return Mono.just(MemoryRetrievalResult.empty());
			});
	}

	private int calculateSimilarity(String query, String candidate) {
		Set<String> queryWords = tokenize(query);
		Set<String> candidateWords = tokenize(candidate);
		Set<String> intersection = new HashSet<>(queryWords);
		intersection.retainAll(candidateWords);
		return intersection.size();
	}

	private Set<String> tokenize(String text) {
		if (text == null || text.isBlank()) {
			return Set.of();
		}
		return Arrays.stream(text.toLowerCase().split("\\s+"))
			.filter(word -> !word.isEmpty())
			.collect(Collectors.toSet());
	}
}
