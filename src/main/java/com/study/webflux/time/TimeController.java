package com.study.webflux.time;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

/**
 * 시간 정보를 SSE로 스트리밍하거나 카운터를 보여주는 WebFlux 예제 컨트롤러.
 */
@RestController
@RequestMapping("/time")
public class TimeController {

	/**
	 * 매초 현재 시각 문자열을 순차적으로 내보내는 SSE 스트리밍
	 */
	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> timeStream() {
		return Flux.interval(Duration.ofSeconds(1))
			.map(tick -> ZonedDateTime.now().toString());
	}

	/**
	 *  매초 증가하는 숫자를 스트리밍하는 단순 카운터 SSE
	 */
	@GetMapping(path = "/counter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Long> counterStream() {
		return Flux.interval(Duration.ofSeconds(1));
	}
}
