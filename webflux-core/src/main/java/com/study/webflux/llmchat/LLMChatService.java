package com.study.webflux.llmchat;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

@Slf4j
@Service
public class LLMChatService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final LLMChatProperties properties;

    public LLMChatService(LLMChatProperties properties,
                          WebClient.Builder webClientBuilder,
                          ObjectMapper objectMapper) {
        Assert.hasText(properties.getApiKey(), "LLM chat requires an OpenAI API key.");
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Flux<ServerSentEvent<String>> streamChat(LLMChatRequest request) {
        var payload = Collections.unmodifiableMap(
                java.util.Map.of(
                        "model", properties.getModel(),
                        "messages", List.of(java.util.Map.of("role", "user", "content", request.prompt())),
                        "stream", true));

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(payload)
                .retrieve()
                .bodyToFlux(String.class)
                .concatMap(this::parseServerSentEvent);
    }

    private Flux<ServerSentEvent<String>> parseServerSentEvent(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Flux.empty();
        }

        String trimmed = raw.trim();
        if ("[DONE]".equals(trimmed)) {
            return Flux.empty();
        }

        return Flux.fromIterable(extractContents(trimmed)).map(content ->
                ServerSentEvent.builder(content)
                        .event("message")
                        .build());
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
            log.warn("Failed to parse OpenAI stream chunk", e);
            return Collections.emptyList();
        }
    }
}
