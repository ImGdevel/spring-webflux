package com.study.webflux.rag.domain.port.out;

import com.study.webflux.rag.domain.model.conversation.ConversationTurn;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ConversationRepository {
	Mono<ConversationTurn> save(ConversationTurn turn);

	Flux<ConversationTurn> findRecent(int limit);

	Flux<ConversationTurn> findAll();
}
