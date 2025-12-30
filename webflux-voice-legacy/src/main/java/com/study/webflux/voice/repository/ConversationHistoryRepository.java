package com.study.webflux.voice.repository;

import java.time.Instant;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.study.webflux.voice.model.ConversationMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ConversationHistoryRepository extends ReactiveMongoRepository<ConversationMessage, String> {

	Flux<ConversationMessage> findTop10ByOrderByCreatedAtDesc();

	default Mono<ConversationMessage> saveQuery(String query) {
		ConversationMessage message = new ConversationMessage(null, query, Instant.now());
		return save(message);
	}
}
