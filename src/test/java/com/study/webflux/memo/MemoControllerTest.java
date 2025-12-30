package com.study.webflux.memo;

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
class MemoControllerTest {

	@LocalServerPort
	private int port;

	@Test
	void createAndFetchMemo() {
		WebTestClient client = WebTestClient.bindToServer()
			.baseUrl("http://localhost:" + port)
			.responseTimeout(Duration.ofSeconds(5))
			.build();

		MemoRequest request = new MemoRequest("test memo");

		Memo created = client.post()
			.uri("/memos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(request), MemoRequest.class)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(Memo.class)
			.returnResult()
			.getResponseBody();

		assertThat(created).isNotNull();
		assertThat(created.id()).isNotNull();
		assertThat(created.content()).isEqualTo("test memo");

		List<Memo> all = client.get()
			.uri("/memos")
			.exchange()
			.expectStatus().isOk()
			.returnResult(Memo.class)
			.getResponseBody()
			.collectList()
			.block(Duration.ofSeconds(5));

		assertThat(all).isNotNull();
		assertThat(all).extracting(Memo::id).contains(created.id());

		Memo byId = client.get()
			.uri("/memos/{id}", created.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody(Memo.class)
			.returnResult()
			.getResponseBody();

		assertThat(byId).isNotNull();
		assertThat(byId.id()).isEqualTo(created.id());
	}
}

