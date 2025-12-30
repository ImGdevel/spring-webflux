package com.study.webflux.chat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

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

		org.assertj.core.api.Assertions.assertThat(created).isNotNull();
		org.assertj.core.api.Assertions.assertThat(created.user()).isEqualTo("tester");
		org.assertj.core.api.Assertions.assertThat(created.message()).isEqualTo("hello webflux");
	}
}
