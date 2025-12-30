package com.study.webflux.trace;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * 요청/응답 헤더와 Reactor Context 에 traceId 를 주입하는 WebFlux {@link WebFilter}.
 *
 * <p>
 * - 요청 헤더 {@code X-Trace-Id} 가 있으면 그대로 사용하고, 없으면 UUID 로 새로 생성한다. <br>
 * - 응답 헤더에도 같은 {@code X-Trace-Id} 를 추가하고, Reactor Context 에도 {@link #TRACE_ID_KEY} 로 저장한다.
 * </p>
 */
@Component
public class TraceIdFilter implements WebFilter {

	public static final String TRACE_ID_KEY = "traceId";
	private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);

	/**
	 * 각 요청에 traceId 를 부여하고, 이후 체인과 Reactor 연산자에서 사용할 수 있도록 Context 에 저장한다.
	 *
	 * @param exchange 현재 HTTP 요청/응답 교환 객체
	 * @param chain 다음 필터 체인
	 * @return 필터 체인 실행 결과
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String candidate = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
		final String traceId = StringUtils.hasText(candidate) ? candidate : UUID.randomUUID().toString();

		log.debug("Trace filter assigned traceId={}", traceId);
		exchange.getResponse().getHeaders().add("X-Trace-Id", traceId);

		return chain.filter(exchange)
			.contextWrite(ctx -> ctx.put(TRACE_ID_KEY, traceId));
	}
}
