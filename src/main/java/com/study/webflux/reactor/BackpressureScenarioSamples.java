package com.study.webflux.reactor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.reactivestreams.Subscription;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * 테스트나 간단한 데모에서 바로 실행할 수 있는 다양한 백프레셔 시나리오 모음.
 */
public class BackpressureScenarioSamples {

	/**
	 * BaseSubscriber를 이용해 직접 request(n)을 보내면서 배치 크기를 상황에 맞게 증감시키는 시나리오.
	 * 각 단계에서 어떤 배치 크기, 어떤 request 신호가 발생했는지를 ManualDemandResult에 담아준다.
	 */
	public ManualDemandResult runManualDemandScenario(
		int maxValue,
		int initialBatchSize,
		int maxBatchSize,
		Duration processingDelay
	) {
		if (maxValue <= 0) {
			throw new IllegalArgumentException("maxValue must be positive");
		}
		if (initialBatchSize <= 0 || maxBatchSize < initialBatchSize) {
			throw new IllegalArgumentException("Batch sizes must be positive and max >= initial");
		}

		List<Integer> consumedValues = new CopyOnWriteArrayList<>();
		List<Long> requestSignals = new CopyOnWriteArrayList<>();
		List<Integer> batchHistory = new CopyOnWriteArrayList<>();
		CountDownLatch latch = new CountDownLatch(1);

		Flux<Integer> source = Flux.range(1, maxValue)
			.hide()
			.doOnRequest(requestSignals::add);

		BaseSubscriber<Integer> adaptiveSubscriber = new BaseSubscriber<>() {
			private int currentBatch = initialBatchSize;
			private int remainingInBatch = initialBatchSize;

			@Override
			protected void hookOnSubscribe(Subscription subscription) {
				batchHistory.add(currentBatch);
				request(currentBatch);
			}

			@Override
			protected void hookOnNext(Integer value) {
				consumedValues.add(value);
				delayIfNecessary(processingDelay);

				if (--remainingInBatch == 0) {
					adjustBatchSize(value);
					batchHistory.add(currentBatch);
					remainingInBatch = currentBatch;
					request(currentBatch);
				}
			}

			@Override
			protected void hookOnComplete() {
				latch.countDown();
			}

			@Override
			protected void hookOnError(Throwable throwable) {
				latch.countDown();
			}

			private void adjustBatchSize(Integer value) {
				if (value % 4 == 0 && currentBatch < maxBatchSize) {
					currentBatch++;
				} else if (value % 7 == 0 && currentBatch > 1) {
					currentBatch--;
				}
			}
		};

		source.subscribe(adaptiveSubscriber);

		await(latch, Duration.ofSeconds(5));

		return new ManualDemandResult(consumedValues, requestSignals, batchHistory);
	}

	/**
	 * 같은 입력을 onBackpressureDrop / onBackpressureLatest 로 흘려보내 결과 차이를 비교한다.
	 */
	public OverflowStrategyResult runOverflowStrategyScenario(int elements, Duration consumerDelay) {
		if (elements <= 0) {
			throw new IllegalArgumentException("elements must be positive");
		}

		Duration safeDelay = consumerDelay == null ? Duration.ZERO : consumerDelay;

		List<Integer> dropConsumed = new CopyOnWriteArrayList<>();
		List<Integer> dropped = new CopyOnWriteArrayList<>();
		List<Integer> latestConsumed = new CopyOnWriteArrayList<>();

		runDropScenario(elements, safeDelay, dropConsumed, dropped);
		runLatestScenario(elements, safeDelay, latestConsumed);

		return new OverflowStrategyResult(dropConsumed, dropped, latestConsumed);
	}

	/**
	 * bufferTimeout 조건(개수/시간) 중 먼저 만족하는 시점에 배치를 플러시하는 패턴을 확인한다.
	 */
	public BufferFlushResult runBufferTimeoutScenario(
		int elements,
		int bufferSize,
		Duration timeout,
		Duration producerDelay
	) {
		if (elements <= 0) {
			throw new IllegalArgumentException("elements must be positive");
		}
		if (bufferSize <= 0) {
			throw new IllegalArgumentException("bufferSize must be positive");
		}

		List<List<Integer>> flushedBatches = new CopyOnWriteArrayList<>();
		Duration safeTimeout = timeout == null ? Duration.ofMillis(1) : timeout;
		Duration safeDelay = producerDelay == null ? Duration.ZERO : producerDelay;

		Flux.range(1, elements)
			.delayElements(safeDelay)
			.bufferTimeout(bufferSize, safeTimeout)
			.doOnNext(batch -> flushedBatches.add(List.copyOf(batch)))
			.blockLast();

		return new BufferFlushResult(flushedBatches);
	}

	private void runDropScenario(
		int elements,
		Duration consumerDelay,
		List<Integer> dropConsumed,
		List<Integer> dropped
	) {
		createHotSource(elements)
			.onBackpressureDrop(dropped::add)
			.publishOn(scheduler())
			.doOnNext(value -> {
				dropConsumed.add(value);
				delayIfNecessary(consumerDelay);
			})
			.blockLast();
	}

	private void runLatestScenario(
		int elements,
		Duration consumerDelay,
		List<Integer> latestConsumed
	) {
		createHotSource(elements)
			.onBackpressureLatest()
			.publishOn(scheduler())
			.doOnNext(value -> {
				latestConsumed.add(value);
				delayIfNecessary(consumerDelay);
			})
			.blockLast();
	}

	private Flux<Integer> createHotSource(int elements) {
		return Flux.create(
			sink -> {
				IntStream.rangeClosed(1, elements).forEach(sink::next);
				sink.complete();
			},
			FluxSink.OverflowStrategy.IGNORE
		);
	}

	private void delayIfNecessary(Duration processingDelay) {
		if (processingDelay == null || processingDelay.isZero() || processingDelay.isNegative()) {
			return;
		}

		try {
			Thread.sleep(processingDelay.toMillis());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void await(CountDownLatch latch, Duration timeout) {
		try {
			if (!latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
				throw new IllegalStateException("Scenario did not finish within timeout");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting for scenario to finish", e);
		}
	}

	private Scheduler scheduler() {
		return Schedulers.boundedElastic();
	}

	public record ManualDemandResult(
		List<Integer> consumedValues,
		List<Long> requestSignals,
		List<Integer> batchHistory
	) {
	}

	public record OverflowStrategyResult(
		List<Integer> dropConsumed,
		List<Integer> droppedValues,
		List<Integer> latestConsumed
	) {
	}

	public record BufferFlushResult(
		List<List<Integer>> flushedBatches
	) {
	}
}
