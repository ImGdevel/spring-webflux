package com.study.webflux.rag.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.webflux.rag.domain.port.out.LlmPort;
import com.study.webflux.rag.infrastructure.adapter.llm.OpenAiConfig;
import com.study.webflux.rag.infrastructure.adapter.llm.OpenAiLlmAdapter;
import com.study.webflux.rag.infrastructure.config.properties.RagDialogueProperties;

@Configuration
public class LlmConfiguration {

	@Bean
	public OpenAiConfig openAiConfig(RagDialogueProperties properties) {
		var openai = properties.getOpenai();
		return new OpenAiConfig(openai.getApiKey(), openai.getBaseUrl(), openai.getModel());
	}

	@Bean
	public LlmPort llmPort(WebClient.Builder webClientBuilder, ObjectMapper objectMapper, OpenAiConfig config) {
		return new OpenAiLlmAdapter(webClientBuilder, objectMapper, config);
	}
}
