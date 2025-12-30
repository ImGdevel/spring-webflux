package com.study.webflux.time;

import java.time.Duration;
import java.time.ZonedDateTime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.webflux.trace.TraceIdFilter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link TraceIdFilter} 가 넣어준 traceId 를 Reactor Context 로 꺼내면서
 * 시간/카운터를 SSE 스트림으로 제공하는 WebFlux 예제 컨트롤러.
 *
 * <p>
 * - {@link #timeStream()}: 현재 시각 문자열을 1초 간격으로 스트리밍. <br>
 * - {@link #counterStream()}: 0부터 시작하는 숫자 카운터를 1초 간격으로 스트리밍.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/time")
public class TimeController {

	/**
	 * 매초 현재 시각 문자열을 순차적으로 내보내는 SSE 스트리밍
	 */
	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> timeStream() {
		return Flux.interval(Duration.ofSeconds(1))
			.flatMap(tick -> Mono.deferContextual(ctx -> {
				String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "none");
				log.info("time.tick={} traceId={}", tick, traceId);
				return Mono.just(ZonedDateTime.now().toString() + " trace=" + traceId);
			}));
	}

	/**
	 *  매초 증가하는 숫자를 스트리밍하는 단순 카운터 SSE
	 */
	@GetMapping(path = "/counter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> counterStream() {
		return Flux.interval(Duration.ofSeconds(1))
			.flatMap(tick -> Mono.deferContextual(ctx -> {
				String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "none");
				log.info("counter.tick={} traceId={}", tick, traceId);
				return Mono.just("count=" + tick + " trace=" + traceId);
			}));
	}
}
