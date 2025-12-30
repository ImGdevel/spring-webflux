package com.study.webflux.rag.voice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.study.webflux.rag.voice.client.FakeLlmStreamingClient;
import com.study.webflux.rag.voice.client.FakeTtsStreamingClient;
import com.study.webflux.rag.voice.client.LlmStreamingClient;
import com.study.webflux.rag.voice.client.TtsStreamingClient;
import com.study.webflux.rag.voice.model.RagVoiceRequest;

import reactor.core.publisher.Flux;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class RagVoiceControllerTest {

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		LlmStreamingClient testLlmClient() {
			return new FakeLlmStreamingClient();
		}

		@Bean
		@Primary
		TtsStreamingClient testTtsClient() {
			return new FakeTtsStreamingClient();
		}
	}

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void ragVoiceEndpointStreamsBase64AudioChunks() {
		RagVoiceRequest request = new RagVoiceRequest("WebFlux란 무엇인가?", Instant.now());

		FluxExchangeResult<String> response = webTestClient.post()
			.uri("/rag/voice/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.TEXT_EVENT_STREAM)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.returnResult(String.class);

		Flux<String> body = response.getResponseBody().take(5);
		List<String> chunks = body.collectList().block(Duration.ofSeconds(10));

		assertNotNull(chunks);
		assertEquals(5, chunks.size());

		String decodedFirstChunk = new String(Base64.getDecoder().decode(chunks.get(0)), StandardCharsets.UTF_8);
		assertTrue(decodedFirstChunk.contains("AUDIO-CHUNK-1"));
	}

	@Test
	void ragVoiceEndpointUsesConversationHistory() {
		webTestClient.post()
			.uri("/rag/voice/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(new RagVoiceRequest("첫 번째 질문입니다", Instant.now()))
			.exchange()
			.expectStatus().isOk()
			.returnResult(String.class)
			.getResponseBody()
			.take(1)
			.blockLast(Duration.ofSeconds(5));

		webTestClient.post()
			.uri("/rag/voice/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(new RagVoiceRequest("두 번째 질문입니다", Instant.now()))
			.exchange()
			.expectStatus().isOk()
			.returnResult(String.class)
			.getResponseBody()
			.take(1)
			.blockLast(Duration.ofSeconds(5));

		RagVoiceRequest thirdRequest = new RagVoiceRequest("첫 번째 관련 질문", Instant.now());
		FluxExchangeResult<String> response = webTestClient.post()
			.uri("/rag/voice/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.TEXT_EVENT_STREAM)
			.bodyValue(thirdRequest)
			.exchange()
			.expectStatus().isOk()
			.returnResult(String.class);

		List<String> chunks = response.getResponseBody()
			.take(1)
			.collectList()
			.block(Duration.ofSeconds(5));

		assertNotNull(chunks);
		assertTrue(chunks.size() > 0);
	}
}
