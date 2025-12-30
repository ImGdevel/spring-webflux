package com.study.webflux.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

class BackpressureExampleTest {

	@Test
	void 기본구독자가_작은배치로_요청한다() {
		List<Long> requestSignals = new CopyOnWriteArrayList<>();
		MeteredSubscriber subscriber = new MeteredSubscriber(2, Duration.ofMillis(5));

		Flux<Integer> fastProducer = Flux.range(1, 6)
			.hide() // disable synchronous fusion so we can observe each request
			.doOnRequest(requestSignals::add);

		fastProducer.subscribe(subscriber);

		assertEquals(List.of(2L, 2L, 2L, 2L), requestSignals);
		assertEquals(List.of(1, 2, 3, 4, 5, 6), subscriber.getConsumedValues());
	}

	@Test
	void 드랍전략이_과잉데이터를_버린다() {
		List<Integer> dropped = new CopyOnWriteArrayList<>();
		List<Integer> consumed = new CopyOnWriteArrayList<>();

		Flux<Integer> hotSource = Flux.create(
			sink -> {
				IntStream.rangeClosed(1, 1000).forEach(sink::next);
				sink.complete();
			},
			FluxSink.OverflowStrategy.IGNORE // ignore downstream demand to simulate an uncontrollable producer
		);

		hotSource
			.onBackpressureDrop(dropped::add)
			.publishOn(Schedulers.boundedElastic())
			.doOnNext(value -> {
				consumed.add(value);
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			})
			.blockLast();

		assertFalse(dropped.isEmpty(), "과잉 데이터가 드랍되어야 한다");
		assertTrue(consumed.size() + dropped.size() == 1000);
	}

	private static final class MeteredSubscriber extends BaseSubscriber<Integer> {

		private final int batchSize;
		private final Duration processingDelay;
		private final List<Integer> consumedValues = new CopyOnWriteArrayList<>();
		private final AtomicInteger remainingInBatch = new AtomicInteger();

		private MeteredSubscriber(int batchSize, Duration processingDelay) {
			this.batchSize = batchSize;
			this.processingDelay = processingDelay;
		}

		@Override
		protected void hookOnSubscribe(Subscription subscription) {
			remainingInBatch.set(batchSize);
			request(batchSize);
		}

		@Override
		protected void hookOnNext(Integer value) {
			consumedValues.add(value);

			if (!processingDelay.isZero() && !processingDelay.isNegative()) {
				try {
					Thread.sleep(processingDelay.toMillis());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			if (remainingInBatch.decrementAndGet() == 0) {
				remainingInBatch.set(batchSize);
				request(batchSize);
			}
		}

		List<Integer> getConsumedValues() {
			return consumedValues;
		}
	}
}
