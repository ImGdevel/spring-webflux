package com.study.webflux.reactor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import reactor.test.StepVerifier;

class BackpressureDemoServiceTest {

	private final BackpressureDemoService service = new BackpressureDemoService();

	@Test
	void limitRate스트림이_요청과데이터를_출력한다() {
		ListAppender<ILoggingEvent> appender = 시작로그캡처();

		StepVerifier.create(service.limitRateStream().collectList().flux())
			.assertNext(messages -> {
				long requestCount = messages.stream().filter(msg -> msg.startsWith("요청 신호")).count();
				long dataCount = messages.stream().filter(msg -> msg.startsWith("데이터")).count();
				assertTrue(requestCount > 0, "요청 로그가 최소 한 번은 출력되어야 한다");
				assertEquals(40, dataCount, "limitRate 시에도 전체 데이터는 모두 전달된다");
			})
			.verifyComplete();

		List<String> logs = 종료로그캡처(appender);
		assertTrue(
			logs.stream().anyMatch(msg -> msg.contains("요청제한 요청 수신")),
			"요청 정보를 실제 로그로 확인해야 한다"
		);
		assertTrue(
			logs.stream().anyMatch(msg -> msg.contains("요청제한 데이터 방출")),
			"데이터 방출 로그가 있어야 한다"
		);
	}

	@Test
	void 드랍전략스트림이_드랍로그를_남긴다() {
		ListAppender<ILoggingEvent> appender = 시작로그캡처();

		StepVerifier.create(service.dropHotStream().collectList().flux())
			.assertNext(messages -> {
				boolean hasDrop = messages.stream().anyMatch(msg -> msg.startsWith("드랍"));
				boolean hasConsume = messages.stream().anyMatch(msg -> msg.startsWith("소비"));
				assertTrue(hasConsume, "실제 소비된 데이터가 있어야 한다");
				assertTrue(hasDrop, "느린 소비자 때문에 드랍된 데이터도 보여야 한다");
			})
			.verifyComplete();

		List<String> logs = 종료로그캡처(appender);
		assertTrue(
			logs.stream().anyMatch(msg -> msg.contains("드랍 발생")),
			"드랍 시점 로그가 남아야 한다"
		);
		assertTrue(
			logs.stream().anyMatch(msg -> msg.contains("소비 완료")),
			"소비 완료 로그가 남아야 한다"
		);
	}

	@Test
	void 배치시나리오가_로그와함께_플러시된다() {
		ListAppender<ILoggingEvent> appender = 시작로그캡처();

		StepVerifier.create(service.bufferedBatchStream().collectList().flux())
			.assertNext(messages -> {
				assertFalse(messages.isEmpty(), "최소 한 번의 배치 플러시가 있어야 한다");
				assertTrue(messages.stream().allMatch(msg -> msg.startsWith("배치 전송")));
			})
			.verifyComplete();

		List<String> logs = 종료로그캡처(appender);
		assertTrue(
			logs.stream().anyMatch(msg -> msg.contains("배치 생성")),
			"배치 생성 로그가 남아야 한다"
		);
	}

	private ListAppender<ILoggingEvent> 시작로그캡처() {
		Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(BackpressureDemoService.class);
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
		return appender;
	}

	private List<String> 종료로그캡처(ListAppender<ILoggingEvent> appender) {
		Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(BackpressureDemoService.class);
		logger.detachAppender(appender);
		appender.stop();
		return appender.list.stream()
			.map(ILoggingEvent::getFormattedMessage)
			.collect(Collectors.toList());
	}
}
