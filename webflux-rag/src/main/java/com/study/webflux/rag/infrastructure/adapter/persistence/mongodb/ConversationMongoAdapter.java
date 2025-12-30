package com.study.webflux.rag.infrastructure.adapter.persistence.mongodb;

import org.springframework.stereotype.Component;

import com.study.webflux.rag.domain.model.conversation.ConversationTurn;
import com.study.webflux.rag.domain.port.out.ConversationRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ConversationMongoAdapter implements ConversationRepository {

	private final ConversationMongoRepository mongoRepository;

	public ConversationMongoAdapter(ConversationMongoRepository mongoRepository) {
		this.mongoRepository = mongoRepository;
	}

	@Override
	public Mono<ConversationTurn> save(ConversationTurn turn) {
		ConversationEntity entity = new ConversationEntity(turn.id(), turn.query(), turn.createdAt());
		return mongoRepository.save(entity)
			.map(saved -> ConversationTurn.withId(saved.id(), saved.query(), saved.createdAt()));
	}

	@Override
	public Flux<ConversationTurn> findRecent(int limit) {
		return mongoRepository.findAll()
			.takeLast(limit)
			.map(entity -> ConversationTurn.withId(entity.id(), entity.query(), entity.createdAt()));
	}

	@Override
	public Flux<ConversationTurn> findAll() {
		return mongoRepository.findAll()
			.map(entity -> ConversationTurn.withId(entity.id(), entity.query(), entity.createdAt()));
	}
}
