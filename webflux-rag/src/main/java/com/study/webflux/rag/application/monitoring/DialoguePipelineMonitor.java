package com.study.webflux.rag.application.monitoring;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DialoguePipelineMonitor {

	private final PipelineMetricsReporter reporter;
	private final Clock clock;

	@Autowired
	public DialoguePipelineMonitor(PipelineMetricsReporter reporter) {
		this(reporter, Clock.systemUTC());
	}

	DialoguePipelineMonitor(PipelineMetricsReporter reporter, Clock clock) {
		this.reporter = reporter;
		this.clock = clock;
	}

	public DialoguePipelineTracker create(String inputText) {
		return new DialoguePipelineTracker(inputText, reporter, clock);
	}
}
