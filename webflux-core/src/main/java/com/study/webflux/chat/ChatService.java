package com.study.webflux.chat;

import java.time.Instant;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * {@link Sinks.Many} 를 사용해서 채팅 메시지를 브로드캐스트하는 서비스.
 *
 * <p>
 * - {@link #publish(ChatMessageRequest)}: 요청을 {@link ChatMessage} 로 변환해서 sink 에 발행한다. <br>
 * - {@link #stream()}: 발행된 메시지를 구독할 수 있는 {@link Flux} 를 노출한다.
 * </p>
 */
@Service
public class ChatService {

	private final Sinks.Many<ChatMessage> sink = Sinks.many().multicast().onBackpressureBuffer();

	/**
	 * 채팅 요청을 받아 {@link ChatMessage} 로 변환하고 sink 로 발행한다.
	 *
	 * @param request 사용자와 메시지 텍스트를 담은 요청
	 * @return 방금 발행한 {@link ChatMessage}
	 */
	public Mono<ChatMessage> publish(ChatMessageRequest request) {
		ChatMessage message = new ChatMessage(request.user(), request.message(), Instant.now());
		sink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
		return Mono.just(message);
	}

	/**
	 * 현재 채팅방에서 발행되는 모든 메시지를 구독하기 위한 스트림을 반환한다.
	 *
	 * @return 실시간 채팅 메시지 스트림
	 */
	public Flux<ChatMessage> stream() {
		return sink.asFlux();
	}
}
