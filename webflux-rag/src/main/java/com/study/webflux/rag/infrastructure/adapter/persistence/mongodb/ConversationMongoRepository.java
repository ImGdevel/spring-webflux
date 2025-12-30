package com.study.webflux.rag.infrastructure.adapter.persistence.mongodb;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;

public interface ConversationMongoRepository extends ReactiveMongoRepository<ConversationEntity, String> {
	Flux<ConversationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

	Flux<ConversationEntity> findTop10ByOrderByCreatedAtDesc();
}
