package com.study.webflux.memo;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class MemoRepository {

	private final Map<Long, Memo> store = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(0);

	public Mono<Memo> save(String content) {
		long id = sequence.incrementAndGet();
		Memo memo = new Memo(id, content, Instant.now());
		store.put(id, memo);
		return Mono.just(memo);
	}

	public Mono<Memo> findById(Long id) {
		return Mono.justOrEmpty(Optional.ofNullable(store.get(id)));
	}

	public Flux<Memo> findAll() {
		Collection<Memo> values = store.values();
		return Flux.fromIterable(values).sort((a, b) -> Long.compare(a.id(), b.id()));
	}
}
