package com.study.webflux.rag.application.monitoring;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingPipelineMetricsReporter implements PipelineMetricsReporter {

	private static final Logger log = LoggerFactory.getLogger(LoggingPipelineMetricsReporter.class);

	@Override
	public void report(VoicePipelineTracker.PipelineSummary summary) {
		String stageSummary = summary.stages().stream()
			.map(stage -> stage.stage() + ":" + stage.status() + "(" + stage.durationMillis() + "ms, attrs=" + stage.attributes() + ")")
			.collect(Collectors.joining(", "));

		log.info(
			"Voice pipeline {} finished status={} duration={}ms attributes={} stages=[{}]",
			summary.pipelineId(),
			summary.status(),
			summary.durationMillis(),
			summary.attributes(),
			stageSummary
		);
	}
}
