package com.study.webflux.reactor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class BackpressureScenarioSamplesTest {

	private final BackpressureScenarioSamples samples = new BackpressureScenarioSamples();

	@Test
	void 수동요청시나리오에서_배치크기가_적응한다() {
		BackpressureScenarioSamples.ManualDemandResult result =
			samples.runManualDemandScenario(20, 2, 5, Duration.ofMillis(2));

		assertEquals(20, result.consumedValues().size());
		assertTrue(result.batchHistory().stream().anyMatch(size -> size > 2), "배치 크기가 최소 한 번은 늘어나야 한다");
		assertTrue(result.requestSignals().size() >= 5, "여러 번의 요청 파동이 기록되어야 한다");
	}

	@Test
	void 드랍과최신전략의_결과가_구분된다() {
		BackpressureScenarioSamples.OverflowStrategyResult result =
			samples.runOverflowStrategyScenario(500, Duration.ofMillis(1));

		assertFalse(result.droppedValues().isEmpty(), "드랍 전략에서는 버려지는 데이터가 있어야 한다");
		assertTrue(result.latestConsumed().contains(500), "최신 전략에서는 마지막 데이터가 살아 있어야 한다");
		assertTrue(result.latestConsumed().size() > result.dropConsumed().size(), "최신 전략이 더 많은 데이터를 유지해야 한다");
	}

	@Test
	void 버퍼타임아웃이_모든데이터를_플러시한다() {
		BackpressureScenarioSamples.BufferFlushResult result =
			samples.runBufferTimeoutScenario(25, 6, Duration.ofMillis(10), Duration.ofMillis(1));

		int total = result.flushedBatches().stream().mapToInt(batch -> batch == null ? 0 : batch.size()).sum();
		assertEquals(25, total);
		assertTrue(result.flushedBatches().size() >= 4, "여러 차례 배치 전송이 발생해야 한다");
	}
}
