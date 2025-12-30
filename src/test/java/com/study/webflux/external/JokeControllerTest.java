package com.study.webflux.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JokeControllerTest {

	@LocalServerPort
	private int port;

	@MockBean
	private JokeService jokeService;

	@Test
	void jokeEndpointUsesServiceAndPrefixesLanguage() {
		WebTestClient client = WebTestClient.bindToServer()
			.baseUrl("http://localhost:" + port)
			.responseTimeout(Duration.ofSeconds(5))
			.build();

		JokeResponse base = new JokeResponse("id-1", "sample joke", "http://example.com", "http://icon");
		given(jokeService.randomJoke()).willReturn(Mono.just(base));

		JokeResponse result = client.get()
			.uri("/external/joke")
			.accept(MediaType.APPLICATION_JSON)
			.header("Accept-Language", "ko-KR")
			.exchange()
			.expectStatus().isOk()
			.expectBody(JokeResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo("id-1");
		assertThat(result.value()).startsWith("[lang=ko-KR] ");

		Mockito.verify(jokeService).randomJoke();
	}
}

