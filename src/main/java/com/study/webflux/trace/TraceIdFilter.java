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

@Component
public class TraceIdFilter implements WebFilter {

	public static final String TRACE_ID_KEY = "traceId";
	private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);

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
