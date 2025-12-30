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

/**
 * 인메모리 Map 을 사용해 메모를 저장하는 간단한 리액티브 리포지토리.
 *
 * <p>
 * 실제 DB 대신 {@link ConcurrentHashMap} 과 {@link AtomicLong} 시퀀스를 사용해서
 * CRUD 패턴과 {@link Mono}/{@link Flux} 사용법을 익히는 것이 목적이다.
 * </p>
 */
@Repository
public class MemoRepository {

	private final Map<Long, Memo> store = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(0);

	/**
	 * 새 메모를 저장하고 바로 결과를 반환한다.
	 *
	 * @param content 메모 내용
	 * @return 생성된 ID 와 생성 시각이 채워진 {@link Memo}
	 */
	public Mono<Memo> save(String content) {
		long id = sequence.incrementAndGet();
		Memo memo = new Memo(id, content, Instant.now());
		store.put(id, memo);
		return Mono.just(memo);
	}

	/**
	 * ID 로 단일 메모를 조회한다.
	 *
	 * @param id 메모 ID
	 * @return 존재하면 해당 메모, 없으면 비어 있는 {@link Mono}
	 */
	public Mono<Memo> findById(Long id) {
		return Mono.justOrEmpty(Optional.ofNullable(store.get(id)));
	}

	/**
	 * 저장된 모든 메모를 ID 오름차순으로 스트리밍한다.
	 *
	 * @return 모든 메모의 {@link Flux} 스트림
	 */
	public Flux<Memo> findAll() {
		Collection<Memo> values = store.values();
		return Flux.fromIterable(values).sort((a, b) -> Long.compare(a.id(), b.id()));
	}
}
