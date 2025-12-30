package com.study.webflux.voice;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

/**
 * TTS 데모용 WebSocket 엔드포인트 구성을 담당하는 설정 클래스.
 */
@Configuration
public class TtsWebSocketConfig {

	@Bean
	public HandlerMapping ttsWebSocketMapping(TtsWebSocketHandler handler) {
		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		mapping.setOrder(-1);
		mapping.setUrlMap(Map.of(
			"/ws/voice/tts", handler
		));
		return mapping;
	}

	@Bean
	public WebSocketHandlerAdapter webSocketHandlerAdapter() {
		return new WebSocketHandlerAdapter();
	}
}
