package com.study.webflux.rag.infrastructure.adapter.persistence.mongodb;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ConversationMongoRepository extends ReactiveMongoRepository<ConversationEntity, String> {
}
