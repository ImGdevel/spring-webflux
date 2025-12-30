package com.study.webflux.rag.infrastructure.adapter.llm;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.webflux.rag.domain.model.llm.CompletionRequest;
import com.study.webflux.rag.domain.port.out.LlmPort;
import com.study.webflux.rag.infrastructure.adapter.llm.dto.OpenAiRequest;
import com.study.webflux.rag.infrastructure.adapter.llm.dto.OpenAiStreamResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OpenAiLlmAdapter implements LlmPort {

	private final WebClient webClient;
	private final ObjectMapper objectMapper;

	public OpenAiLlmAdapter(WebClient.Builder webClientBuilder, ObjectMapper objectMapper, OpenAiConfig config) {
		this.objectMapper = objectMapper;
		this.webClient = webClientBuilder
			.baseUrl(config.baseUrl())
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.apiKey())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	@Override
	public Flux<String> streamCompletion(CompletionRequest request) {
		OpenAiRequest openAiRequest = OpenAiRequest.from(request);

		return webClient.post()
			.uri("/chat/completions")
			.bodyValue(openAiRequest)
			.retrieve()
			.bodyToFlux(String.class)
			.concatMap(this::parseStreamChunk);
	}

	@Override
	public Mono<String> complete(CompletionRequest request) {
		return streamCompletion(request)
			.collect(StringBuilder::new, StringBuilder::append)
			.map(StringBuilder::toString);
	}

	private Flux<String> parseStreamChunk(String raw) {
		if (!StringUtils.hasText(raw)) {
			return Flux.empty();
		}

		String trimmed = raw.trim();
		if ("[DONE]".equals(trimmed)) {
			return Flux.empty();
		}

		if (trimmed.startsWith("data: ")) {
			trimmed = trimmed.substring(6);
		}

		return Flux.fromIterable(extractContents(trimmed));
	}

	private List<String> extractContents(String json) {
		try {
			OpenAiStreamResponse response = objectMapper.readValue(json, OpenAiStreamResponse.class);
			return response.choices().stream()
				.map(OpenAiStreamResponse.OpenAiChoice::delta)
				.filter(delta -> delta != null)
				.map(OpenAiStreamResponse.OpenAiDelta::content)
				.filter(StringUtils::hasText)
				.toList();
		}
		catch (JsonProcessingException e) {
			return Collections.emptyList();
		}
	}
}
