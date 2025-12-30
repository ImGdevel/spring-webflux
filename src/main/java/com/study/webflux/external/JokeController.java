package com.study.webflux.external;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * 외부 Chuck Norris API를 WebClient로 호출하고, 헤더 값을 가공해서 응답하는 WebFlux 예제 컨트롤러.
 *
 * <p>
 * {@link JokeService} 를 이용해 랜덤 조크를 가져오고,
 * 요청 헤더의 {@code Accept-Language} 값을 조합해서 응답 메시지 앞에 붙인다.
 * </p>
 */
@RestController
@RequestMapping("/external")
public class JokeController {

	private final JokeService jokeService;

	public JokeController(JokeService jokeService) {
		this.jokeService = jokeService;
	}

	/**
	 * 외부 Chuck Norris API 에서 랜덤 조크를 받아, 언어 정보를 앞에 붙여 반환한다.
	 *
	 * @param locale {@code Accept-Language} 헤더 값 (없으면 {@code en})
	 * @return 외부 API 결과를 약간 가공한 {@link JokeResponse}
	 */
	@GetMapping("/joke")
	public Mono<JokeResponse> randomJoke(@RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {
		return jokeService.randomJoke()
			.map(joke -> new JokeResponse(joke.id(), "[lang=" + locale + "] " + joke.value(), joke.url(), joke.iconUrl()));
	}
}
