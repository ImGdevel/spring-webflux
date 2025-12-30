package com.study.webflux.voice.client;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.webflux.voice.config.RagVoiceProperties;

import reactor.core.publisher.Flux;

@Component
public class OpenAiLlmStreamingClient implements LlmStreamingClient {

	private final WebClient webClient;
	private final ObjectMapper objectMapper;
	private final RagVoiceProperties properties;

	public OpenAiLlmStreamingClient(
		WebClient.Builder webClientBuilder,
		ObjectMapper objectMapper,
		RagVoiceProperties properties
	) {
		this.objectMapper = objectMapper;
		this.properties = properties;
		this.webClient = webClientBuilder
			.baseUrl(properties.getOpenai().getBaseUrl())
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getOpenai().getApiKey())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	@Override
	public Flux<String> streamCompletion(String prompt) {
		var payload = Collections.unmodifiableMap(
			java.util.Map.of(
				"model", properties.getOpenai().getModel(),
				"messages", List.of(java.util.Map.of("role", "user", "content", prompt)),
				"stream", true));

		return webClient.post()
			.uri("/chat/completions")
			.bodyValue(payload)
			.retrieve()
			.bodyToFlux(String.class)
			.concatMap(this::parseStreamChunk);
	}

	private Flux<String> parseStreamChunk(String raw) {
		if (!StringUtils.hasText(raw)) {
			return Flux.empty();
		}

		String trimmed = raw.trim();
		if ("[DONE]".equals(trimmed)) {
			return Flux.empty();
		}

		return Flux.fromIterable(extractContents(trimmed));
	}

	private List<String> extractContents(String json) {
		try {
			OpenAiStreamResponse response = objectMapper.readValue(json, OpenAiStreamResponse.class);
			return response.choices().stream()
				.map(OpenAiChoice::delta)
				.filter(java.util.Objects::nonNull)
				.map(OpenAiDelta::content)
				.filter(StringUtils::hasText)
				.collect(toList());
		}
		catch (JsonProcessingException e) {
			return Collections.emptyList();
		}
	}
}
