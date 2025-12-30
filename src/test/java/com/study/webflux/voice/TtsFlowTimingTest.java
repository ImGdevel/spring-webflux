package com.study.webflux.voice;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TtsFlowTimingTest {

	@Autowired
	private WebClient.Builder webClientBuilder;

	@LocalServerPort
	private int port;

	@Test
	void ssePipelineEmitsChunksAndTakesTime() {
		String text = "이것은 긴 문장입니다 1234567890";
		WebClient client = webClientBuilder.baseUrl("http://localhost:" + port)
			.exchangeStrategies(ExchangeStrategies.withDefaults()).build();

		Instant start = Instant.now();

		List<String> chunks = client.post()
			.uri("/voice/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(new SseTtsController.LlmRequestBody(text))
			.retrieve()
			.bodyToFlux(String.class)
			.collectList()
			.block();

		Duration duration = Duration.between(start, Instant.now());

		assertThat(chunks).isNotEmpty();
		assertThat(duration).isGreaterThanOrEqualTo(Duration.ofMillis(400));
	}


}
