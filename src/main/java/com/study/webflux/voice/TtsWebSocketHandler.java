package com.study.webflux.voice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 텍스트를 WebSocket으로 받아 LLM → TTS 스트림을 다시 WebSocket 텍스트 메시지로 전달하는 핸들러.
 */
@Component
public class TtsWebSocketHandler implements WebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(TtsWebSocketHandler.class);

	private final FakeLlmService llmService;
	private final FakeTtsService ttsService;

	public TtsWebSocketHandler(FakeLlmService llmService, FakeTtsService ttsService) {
		this.llmService = llmService;
		this.ttsService = ttsService;
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		return session.receive()
			.map(WebSocketMessage::getPayloadAsText)
			.next()
			.flatMapMany(text -> llmService.composeResponse(text)
				.doOnNext(sentence -> log.info("LLM sentence over WebSocket: {}", sentence))
				.flatMapMany(ttsService::synthesizeFullText))
			.map(session::textMessage)
			.as(session::send)
			.then(session.close());
	}
}
