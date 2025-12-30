package com.study.webflux.voice;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
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
	public HandlerMapping ttsWebSocketMapping(TtsWebSocketHandler handler, WebFluxProperties properties) {
		String basePath = properties.getBasePath();
		String prefixedPath = StringUtils.hasText(basePath) ? basePath + "/ws/voice/tts" : null;
		Map<String, WebSocketHandler> urlMap = new LinkedHashMap<>();
		urlMap.put("/ws/voice/tts", handler);
		if (prefixedPath != null) {
			urlMap.put(prefixedPath, handler);
		}

		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		mapping.setOrder(-1);
		mapping.setUrlMap(urlMap);
		return mapping;
	}

	@Bean
	public WebSocketHandlerAdapter webSocketHandlerAdapter() {
		return new WebSocketHandlerAdapter();
	}
}
