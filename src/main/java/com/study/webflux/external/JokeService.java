package com.study.webflux.external;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class JokeService {

	private final WebClient webClient;

	public JokeService(WebClient.Builder builder) {
		this.webClient = builder.baseUrl("https://api.chucknorris.io").build();
	}

	public Mono<JokeResponse> randomJoke() {
		return webClient.get()
			.uri("/jokes/random")
			.retrieve()
			.bodyToMono(JokeResponse.class)
			.onErrorResume(error -> Mono.just(new JokeResponse("unknown", "Unable to fetch a joke right now.", null, null)));
	}
}
