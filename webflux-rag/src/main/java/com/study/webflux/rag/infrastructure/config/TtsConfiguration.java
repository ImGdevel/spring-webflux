package com.study.webflux.rag.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.study.webflux.rag.domain.model.voice.Voice;
import com.study.webflux.rag.domain.port.out.TtsPort;
import com.study.webflux.rag.infrastructure.adapter.tts.SupertoneConfig;
import com.study.webflux.rag.infrastructure.adapter.tts.SupertoneTtsAdapter;
import com.study.webflux.rag.infrastructure.config.properties.RagDialogueProperties;

@Configuration
public class TtsConfiguration {

	@Bean
	public SupertoneConfig supertoneConfig(RagDialogueProperties properties) {
		var supertone = properties.getSupertone();
		return new SupertoneConfig(supertone.getApiKey(), supertone.getBaseUrl());
	}

	@Bean
	public TtsPort ttsPort(WebClient.Builder webClientBuilder, SupertoneConfig config, Voice voice) {
		return new SupertoneTtsAdapter(webClientBuilder, config, voice);
	}
}
