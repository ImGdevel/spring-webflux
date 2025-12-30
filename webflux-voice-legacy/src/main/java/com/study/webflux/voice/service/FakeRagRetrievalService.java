package com.study.webflux.voice.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.study.webflux.voice.model.ConversationMessage;
import com.study.webflux.voice.model.RetrievalResult;
import com.study.webflux.voice.repository.ConversationHistoryRepository;

import reactor.core.publisher.Mono;

@Service
public class FakeRagRetrievalService {

	private final ConversationHistoryRepository repository;

	public FakeRagRetrievalService(ConversationHistoryRepository repository) {
		this.repository = repository;
	}

	public Mono<List<RetrievalResult>> retrieve(String query, int topK) {
		return repository.findAll()
			.collectList()
			.map(messages -> messages.stream()
				.map(msg -> new RetrievalResult(msg, calculateSimilarity(query, msg.query())))
				.filter(result -> result.score() > 0)
				.sorted((a, b) -> Integer.compare(b.score(), a.score()))
				.limit(topK)
				.collect(Collectors.toList())
			);
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
