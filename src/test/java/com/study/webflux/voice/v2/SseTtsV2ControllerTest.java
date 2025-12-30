package com.study.webflux.voice.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SseTtsV2ControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void sseEndpointStreamsBase64AudioChunks() {
		VoiceV2Request request = new VoiceV2Request("테스트 요청입니다.", Instant.now());

		FluxExchangeResult<String> response = webTestClient.post()
			.uri("/voice/v2/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.TEXT_EVENT_STREAM)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.returnResult(String.class);

		Flux<String> body = response.getResponseBody().take(5);
		List<String> chunks = body.collectList().block(Duration.ofSeconds(5));

		assertEquals(5, chunks.size(), "첫 번째 문장의 5개 오디오 청크를 받아야 한다");

		String decodedFirstChunk = new String(Base64.getDecoder().decode(chunks.get(0)), StandardCharsets.UTF_8);
		assertTrue(decodedFirstChunk.contains("AUDIO-CHUNK-1"), "디코딩된 문자열이 AUDIO-CHUNK-1 토큰을 포함해야 한다");
	}
}
