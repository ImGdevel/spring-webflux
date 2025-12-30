package com.study.webflux.external;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * 외부 Chuck Norris API를 WebClient로 호출하고, 헤더 값을 가공해서 응답하는 WebFlux 예제 컨트롤러.
 */
@RestController
@RequestMapping("/external")
public class JokeController {

	private final JokeService jokeService;

	public JokeController(JokeService jokeService) {
		this.jokeService = jokeService;
	}

	@GetMapping("/joke")
	public Mono<JokeResponse> randomJoke(@RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {
		return jokeService.randomJoke()
			.map(joke -> new JokeResponse(joke.id(), "[lang=" + locale + "] " + joke.value(), joke.url(), joke.iconUrl()));
	}
}
