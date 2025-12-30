package com.study.webflux.external;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Chuck Norris 조크 API 를 호출하는 WebClient 기반 서비스.
 *
 * <p>
 * 기본 baseUrl 을 {@code https://api.chucknorris.io} 로 잡고,
 * 네트워크 오류가 발생하면 기본 메시지를 담은 {@link JokeResponse} 로 대체한다.
 * </p>
 */
@Service
public class JokeService {

	private final WebClient webClient;

	public JokeService(WebClient.Builder builder) {
		this.webClient = builder.baseUrl("https://api.chucknorris.io").build();
	}

	/**
	 * 외부 API 에서 랜덤 조크를 한 건 가져온다.
	 *
	 * @return 성공 시 외부 API 응답, 실패 시 "Unable to fetch a joke right now." 메시지를 담은 기본 응답
	 */
	public Mono<JokeResponse> randomJoke() {
		return webClient.get()
			.uri("/jokes/random")
			.retrieve()
			.bodyToMono(JokeResponse.class)
			.onErrorResume(error -> Mono.just(new JokeResponse("unknown", "Unable to fetch a joke right now.", null, null)));
	}
}
