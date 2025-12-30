package com.study.webflux.rag.voice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

@Component
public class OpenAiLlmStreamingClient implements LlmStreamingClient {

	private final WebClient webClient;

	public OpenAiLlmStreamingClient(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder
			.baseUrl("https://api.openai.com/v1")
			.build();
	}

	@Override
	public Flux<String> streamCompletion(String prompt) {
		return Flux.error(new UnsupportedOperationException("OpenAI integration not implemented yet"));
	}
}
