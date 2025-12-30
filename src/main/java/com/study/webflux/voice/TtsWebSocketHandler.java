package com.study.webflux.voice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;

/**
 * 텍스트를 WebSocket으로 받아 LLM → TTS 스트림을 다시 WebSocket 텍스트 메시지로 전달하는 핸들러.
 *
 * <p>
 * - 첫 번째 수신 텍스트 프레임을 프롬프트로 간주해서 {@link FakeLlmService} 에 전달하고, <br>
 * - LLM 이 만든 문장을 {@link FakeTtsService} 로 "오디오 청크" 문자열로 변환해 같은 WebSocket 세션으로 다시 전송한다.
 * </p>
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

	/**
	 * 단일 WebSocket 세션에 대해 텍스트 → LLM → TTS 파이프라인을 실행하고,
	 * 결과를 동일한 세션으로 스트리밍한다.
	 *
	 * @param session 클라이언트와의 WebSocket 세션
	 * @return 파이프라인이 끝나면 완료되는 {@link Mono}
	 */
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
