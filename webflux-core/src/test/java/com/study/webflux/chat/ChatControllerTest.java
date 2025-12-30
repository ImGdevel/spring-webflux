package com.study.webflux.chat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerTest {

	@LocalServerPort
	private int port;

	@Test
	void createChatMessage() {
		WebTestClient client = WebTestClient.bindToServer()
			.baseUrl("http://localhost:" + port)
			.responseTimeout(Duration.ofSeconds(5))
			.build();

		ChatMessageRequest request = new ChatMessageRequest("tester", "hello webflux");

		ChatMessage created = client.post()
			.uri("/chat/messages")
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(request), ChatMessageRequest.class)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ChatMessage.class)
			.returnResult()
			.getResponseBody();

		assertThat(created).isNotNull();
        Assertions.assertNotNull(created);
        assertThat(created.user()).isEqualTo("tester");
		assertThat(created.message()).isEqualTo("hello webflux");
	}

	@Test
	void streamChatMessages() {
		WebTestClient client = WebTestClient.bindToServer()
			.baseUrl("http://localhost:" + port)
			.responseTimeout(Duration.ofSeconds(10))
			.build();

		Flux<ChatMessage> stream = client.get()
			.uri("/chat/stream")
			.accept(MediaType.TEXT_EVENT_STREAM)
			.exchange()
			.expectStatus().isOk()
			.returnResult(ChatMessage.class)
			.getResponseBody();

		ChatMessageRequest request = new ChatMessageRequest("subscriber", "streaming test");

		client.post()
			.uri("/chat/messages")
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(request), ChatMessageRequest.class)
			.exchange()
			.expectStatus().isCreated();

		StepVerifier.create(
			stream.filter(message -> "subscriber".equals(message.user()))
				.take(1)
		)
			.assertNext(message -> {
				assertThat(message.user()).isEqualTo("subscriber");
				assertThat(message.message()).isEqualTo("streaming test");
			})
			.expectComplete()
			.verify(Duration.ofSeconds(5));
	}
}
