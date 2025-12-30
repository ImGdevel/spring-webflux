package com.study.webflux.rag.application.monitoring;

public interface PipelineMetricsReporter {
	void report(VoicePipelineTracker.PipelineSummary summary);
}
