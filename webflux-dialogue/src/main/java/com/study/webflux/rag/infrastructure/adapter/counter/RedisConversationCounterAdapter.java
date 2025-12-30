package com.study.webflux.rag.infrastructure.adapter.counter;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import com.study.webflux.rag.domain.port.out.ConversationCounterPort;

import reactor.core.publisher.Mono;

@Component
public class RedisConversationCounterAdapter implements ConversationCounterPort {

	private static final String COUNTER_KEY = "dialogue:conversation:counter";

	private final ReactiveRedisTemplate<String, Long> redisTemplate;

	public RedisConversationCounterAdapter(ReactiveRedisTemplate<String, Long> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public Mono<Long> increment() {
		return redisTemplate.opsForValue().increment(COUNTER_KEY);
	}

	@Override
	public Mono<Long> get() {
		return redisTemplate.opsForValue()
			.get(COUNTER_KEY)
			.defaultIfEmpty(0L);
	}

	@Override
	public Mono<Void> reset() {
		return redisTemplate.delete(COUNTER_KEY).then();
	}
}
