package com.study.webflux.voice;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SseTtsControllerTest {

	@LocalServerPort
	private int port;

	@Autowired
	private FakeTtsService ttsService;

	@Test
	void sseEndpointStreamsAudioChunks() {
		WebTestClient client = WebTestClient.bindToServer()
			.baseUrl("http://localhost:" + port)
			.responseTimeout(Duration.ofSeconds(10))
			.build();

		SseTtsController.LlmRequestBody body = new SseTtsController.LlmRequestBody("테스트용 긴 문장입니다 1234567890");

		List<String> chunks = client.post()
			.uri("/voice/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.TEXT_EVENT_STREAM)
			.body(Mono.just(body), SseTtsController.LlmRequestBody.class)
			.exchange()
			.expectStatus().isOk()
			.returnResult(String.class)
			.getResponseBody()
			.take(4)
			.collectList()
			.block(Duration.ofSeconds(10));

		assertThat(chunks).isNotNull();
		assertThat(chunks).isNotEmpty();
	}
}

