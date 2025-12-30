package com.study.webflux.reactor;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

/**
 * Spring WebFlux 환경에서 바로 호출해 볼 수 있는 간단한 백프레셔 데모 서비스.
 * 기본적으로 Reactor가 제공하는 백프레셔 기능들을 조합해 문자열 스트림으로 보여준다.
 */
@Service
public class BackpressureDemoService {

	private static final Logger log = LoggerFactory.getLogger(BackpressureDemoService.class);

	/**
	 * limitRate를 이용해 다운스트림이 한 번에 요청하는 개수를 줄이는 데모.
	 * 요청 신호와 실제 데이터 신호를 모두 노출해 이해하기 쉽게 만든다.
	 */
	public Flux<String> limitRateStream() {
		Sinks.Many<String> requestSink = Sinks.many().unicast().onBackpressureBuffer();

		Flux<String> dataStream = Flux.range(1, 40)
			.delayElements(Duration.ofMillis(25))
			.hide()
			.doOnSubscribe(sub -> log.info("요청제한 시나리오 구독 시작"))
			.doOnRequest(n -> {
				log.info("요청제한 요청 수신: {}개", n);
				requestSink.tryEmitNext("요청 신호: " + n + "개 요청");
			})
			.limitRate(5)
			.doOnNext(value -> log.info("요청제한 데이터 방출: {}", value))
			.map(value -> "데이터: " + value);

		return Flux.merge(
			requestSink.asFlux()
				.doFinally(signal -> requestSink.tryEmitComplete()),
			dataStream
		);
	}

	/**
	 * 매우 빠르게 생성되는 Interval 소스를 느린 소비자에게 전달하면서 onBackpressureDrop을 사용.
	 * 어떤 데이터가 드랍됐는지를 dropSink에 노출해 병목을 직관적으로 파악할 수 있게 했다.
	 */
	public Flux<String> dropHotStream() {
		Sinks.Many<String> dropSink = Sinks.many().unicast().onBackpressureBuffer();

		Flux<String> dataStream = Flux.interval(Duration.ofMillis(1))
			.doOnSubscribe(sub -> log.info("드랍 시나리오 구독 시작"))
			.onBackpressureDrop(dropped -> {
				log.warn("드랍 발생: {}", dropped);
				dropSink.tryEmitNext("드랍: " + dropped);
			})
			.publishOn(Schedulers.boundedElastic())
			.delayElements(Duration.ofMillis(6))
			.take(80)
			.doOnNext(value -> log.info("소비 완료: {}", value))
			.map(value -> "소비: " + value)
			.doFinally(signal -> dropSink.tryEmitComplete());

		return Flux.merge(dropSink.asFlux(), dataStream);
	}

	/**
	 * bufferTimeout을 이용해 n개 혹은 특정 시간이 되면 배치로 묶어주는 데모.
	 * 모인 데이터를 문자열로 변환해 어떤 요소들이 하나의 배치로 내려갔는지 쉽게 볼 수 있다.
	 */
	public Flux<String> bufferedBatchStream() {
		return Flux.interval(Duration.ofMillis(30))
			.take(35)
			.bufferTimeout(5, Duration.ofMillis(150))
			.doOnSubscribe(sub -> log.info("배치 시나리오 구독 시작"))
			.doOnNext(batch -> log.info("배치 생성: {}", batch))
			.map(batch -> "배치 전송: " + batch);
	}
}
