package com.study.webflux.chat;

import java.time.Instant;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class ChatService {

	private final Sinks.Many<ChatMessage> sink = Sinks.many().multicast().onBackpressureBuffer();

	public Mono<ChatMessage> publish(ChatMessageRequest request) {
		ChatMessage message = new ChatMessage(request.user(), request.message(), Instant.now());
		sink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
		return Mono.just(message);
	}

	public Flux<ChatMessage> stream() {
		return sink.asFlux();
	}
}
