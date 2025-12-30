package com.study.webflux.rag.application.monitoring;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DialoguePipelineTracker {

	private final String pipelineId;
	private final PipelineMetricsReporter reporter;
	private final Clock clock;
	private final Instant startedAt;
	private final Map<DialoguePipelineStage, StageMetric> stageMetrics = new EnumMap<>(DialoguePipelineStage.class);
	private final Map<String, Object> attributes = new ConcurrentHashMap<>();
	private final AtomicBoolean finished = new AtomicBoolean(false);
	private final List<String> llmOutputs = new CopyOnWriteArrayList<>();
	private final AtomicReference<Instant> firstResponseAt = new AtomicReference<>();
	private final AtomicReference<Instant> lastResponseAt = new AtomicReference<>();
	private volatile Instant finishedAt;

	public DialoguePipelineTracker(String inputText, PipelineMetricsReporter reporter, Clock clock) {
		this.pipelineId = UUID.randomUUID().toString();
		this.reporter = Objects.requireNonNull(reporter, "reporter must not be null");
		this.clock = Objects.requireNonNull(clock, "clock must not be null");
		this.startedAt = clock.instant();
		recordPipelineAttribute("input.length", inputText == null ? 0 : inputText.length());
		recordPipelineAttribute("input.preview", preview(inputText));
	}

	public <T> Mono<T> traceMono(DialoguePipelineStage stage, Supplier<Mono<T>> supplier) {
		Objects.requireNonNull(supplier, "supplier must not be null");
		return Mono.defer(() -> {
			StageMetric metric = stageMetric(stage);
			metric.start(clock.instant());
			Mono<T> publisher = Objects.requireNonNull(supplier.get(), "supplier returned null");
			return publisher
				.doOnSuccess(value -> metric.complete(clock.instant()))
				.doOnError(error -> metric.fail(clock.instant(), error))
				.doOnCancel(() -> metric.cancel(clock.instant()));
		});
	}

	public <T> Flux<T> traceFlux(DialoguePipelineStage stage, Supplier<Flux<T>> supplier) {
		Objects.requireNonNull(supplier, "supplier must not be null");
		return Flux.defer(() -> {
			StageMetric metric = stageMetric(stage);
			metric.start(clock.instant());
			Flux<T> publisher = Objects.requireNonNull(supplier.get(), "supplier returned null");
			return publisher
				.doOnComplete(() -> metric.complete(clock.instant()))
				.doOnError(error -> metric.fail(clock.instant(), error))
				.doOnCancel(() -> metric.cancel(clock.instant()));
		});
	}

	public <T> Flux<T> attachLifecycle(Flux<T> publisher) {
		return publisher
			.doOnComplete(() -> finish(PipelineStatus.COMPLETED, null))
			.doOnError(error -> finish(PipelineStatus.FAILED, error))
			.doOnCancel(() -> finish(PipelineStatus.CANCELLED, null));
	}

	public void recordPipelineAttribute(String key, Object value) {
		if (key != null && value != null) {
			attributes.put(key, value);
		}
	}

	public void recordStageAttribute(DialoguePipelineStage stage, String key, Object value) {
		if (key != null && value != null) {
			stageMetric(stage).putAttribute(key, value);
		}
	}

	public void incrementStageCounter(DialoguePipelineStage stage, String key, long delta) {
		stageMetric(stage).incrementAttribute(key, delta);
	}

	public void recordLlmOutput(String sentence) {
		if (sentence != null && !sentence.isBlank()) {
			llmOutputs.add(sentence);
		}
	}

	public void markResponseEmission() {
		Instant now = clock.instant();
		firstResponseAt.compareAndSet(null, now);
		lastResponseAt.set(now);
	}

	public String pipelineId() {
		return pipelineId;
	}

	private StageMetric stageMetric(DialoguePipelineStage stage) {
		return stageMetrics.computeIfAbsent(stage, StageMetric::new);
	}

	private void finish(PipelineStatus status, Throwable error) {
		if (finished.compareAndSet(false, true)) {
			if (error != null) {
				recordPipelineAttribute("error", error.getMessage());
			}
			this.finishedAt = clock.instant();
			PipelineSummary summary = new PipelineSummary(
				pipelineId,
				status,
				startedAt,
				finishedAt,
				Map.copyOf(attributes),
				stageMetrics.values().stream()
					.map(StageMetric::snapshot)
					.toList(),
				List.copyOf(llmOutputs),
				latencyFromStart(firstResponseAt.get()),
				latencyFromStart(lastResponseAt.get())
			);
			reporter.report(summary);
		}
	}

	private Long latencyFromStart(Instant instant) {
		if (instant == null) {
			return null;
		}
		return Duration.between(startedAt, instant).toMillis();
	}

	private String preview(String text) {
		if (text == null) {
			return "";
		}
		String trimmed = text.trim();
		if (trimmed.length() <= 80) {
			return trimmed;
		}
		return trimmed.substring(0, 77) + "...";
	}

	private static final class StageMetric {
		private final DialoguePipelineStage stage;
		private final Map<String, Object> attributes = new ConcurrentHashMap<>();
		private volatile StageStatus status = StageStatus.PENDING;
		private volatile Instant startedAt;
		private volatile Instant finishedAt;

		private StageMetric(DialoguePipelineStage stage) {
			this.stage = stage;
		}

		private synchronized void start(Instant instant) {
			if (status == StageStatus.PENDING) {
				status = StageStatus.RUNNING;
				startedAt = instant;
			}
		}

		private synchronized void complete(Instant instant) {
			if (status == StageStatus.RUNNING) {
				status = StageStatus.COMPLETED;
				finishedAt = instant;
			}
		}

		private synchronized void fail(Instant instant, Throwable error) {
			if (status == StageStatus.RUNNING || status == StageStatus.PENDING) {
				status = StageStatus.FAILED;
				finishedAt = instant;
				if (error != null) {
					attributes.putIfAbsent("error", error.getMessage());
				}
			}
		}

		private synchronized void cancel(Instant instant) {
			if (status == StageStatus.RUNNING) {
				status = StageStatus.CANCELLED;
				finishedAt = instant;
			}
		}

		private void putAttribute(String key, Object value) {
			attributes.put(key, value);
		}

		private void incrementAttribute(String key, long delta) {
			attributes.compute(key, (k, existing) -> {
				long current = existing instanceof Number ? ((Number)existing).longValue() : 0L;
				return current + delta;
			});
		}

		private StageSnapshot snapshot() {
			long duration = -1L;
			if (startedAt != null && finishedAt != null) {
				duration = Duration.between(startedAt, finishedAt).toMillis();
			}
			return new StageSnapshot(
				stage,
				status,
				startedAt,
				finishedAt,
				duration,
				Map.copyOf(attributes)
			);
		}
	}

	public record StageSnapshot(
		DialoguePipelineStage stage,
		StageStatus status,
		Instant startedAt,
		Instant finishedAt,
		long durationMillis,
		Map<String, Object> attributes
	) {
	}

	public record PipelineSummary(
		String pipelineId,
		PipelineStatus status,
		Instant startedAt,
		Instant finishedAt,
		Map<String, Object> attributes,
		List<StageSnapshot> stages,
		List<String> llmOutputs,
		Long firstResponseLatencyMillis,
		Long lastResponseLatencyMillis
	) {
		public long durationMillis() {
			if (startedAt == null || finishedAt == null) {
				return -1L;
			}
			return Duration.between(startedAt, finishedAt).toMillis();
		}
	}
}
