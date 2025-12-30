package com.study.webflux.rag.application.monitoring;

public interface PipelineMetricsReporter {
	void report(DialoguePipelineTracker.PipelineSummary summary);
}
