package com.study.webflux.voice;

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

	private final FakeLlmService llmService;
	private final FakeTtsService ttsService;

	public TtsWebSocketHandler(FakeLlmService llmService, FakeTtsService ttsService) {
		this.llmService = llmService;
		this.ttsService = ttsService;
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		Flux<WebSocketMessage> out = session.receive()
			.map(WebSocketMessage::getPayloadAsText)
			.flatMap(text -> llmService.composeResponse(text)
				.flatMapMany(ttsService::synthesizeFullText)
				.map(chunk -> session.textMessage(chunk))
			);

		return session.send(out);
	}
}
