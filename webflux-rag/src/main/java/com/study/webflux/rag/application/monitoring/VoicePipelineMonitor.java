package com.study.webflux.rag.application.monitoring;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoicePipelineMonitor {

	private final PipelineMetricsReporter reporter;
	private final Clock clock;

	@Autowired
	public VoicePipelineMonitor(PipelineMetricsReporter reporter) {
		this(reporter, Clock.systemUTC());
	}

	VoicePipelineMonitor(PipelineMetricsReporter reporter, Clock clock) {
		this.reporter = reporter;
		this.clock = clock;
	}

	public VoicePipelineTracker create(String inputText) {
		return new VoicePipelineTracker(inputText, reporter, clock);
	}
}
