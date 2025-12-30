package com.study.webflux.rag.voice.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.study.webflux.rag.voice.model.ConversationMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ConversationHistoryRepository {

	private final Map<Long, ConversationMessage> store = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(0);

	public Mono<ConversationMessage> save(String query) {
		long id = sequence.incrementAndGet();
		ConversationMessage message = new ConversationMessage(id, query, Instant.now());
		store.put(id, message);
		return Mono.just(message);
	}

	public Flux<ConversationMessage> findAll() {
		Collection<ConversationMessage> values = store.values();
		return Flux.fromIterable(values).sort((a, b) -> Long.compare(a.id(), b.id()));
	}
}
