package com.study.webflux.rag.application.monitoring;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingPipelineMetricsReporter implements PipelineMetricsReporter {

	private static final Logger log = LoggerFactory.getLogger(LoggingPipelineMetricsReporter.class);

	@Override
	public void report(DialoguePipelineTracker.PipelineSummary summary) {
		boolean ideaActive = Boolean.parseBoolean(System.getProperty("idea.active", "false"));
		String stageSummary = formatStages(summary, ideaActive);
		String llmOutputs = formatOutputs(summary, ideaActive);

		if (ideaActive) {
			log.info(
				"""
				Dialogue pipeline %s
				  status=%s duration=%sms firstLatency=%sms lastLatency=%sms
				  attributes=%s
				  stages:
				%s
				  llmResults:
				%s""".formatted(
					summary.pipelineId(),
					summary.status(),
					summary.durationMillis(),
					safeLatency(summary.firstResponseLatencyMillis()),
					safeLatency(summary.lastResponseLatencyMillis()),
					summary.attributes(),
					stageSummary,
					llmOutputs
				)
			);
		}
		else {
			log.info(
				"Dialogue pipeline {} status={} duration={}ms firstLatency={}ms lastLatency={}ms attributes={} stages=[{}] llmResults={}",
				summary.pipelineId(),
				summary.status(),
				summary.durationMillis(),
				safeLatency(summary.firstResponseLatencyMillis()),
				safeLatency(summary.lastResponseLatencyMillis()),
				summary.attributes(),
				stageSummary,
				llmOutputs
			);
		}
	}

	private String safeLatency(Long latency) {
		return latency == null ? "-" : latency.toString();
	}

	private String formatStages(DialoguePipelineTracker.PipelineSummary summary, boolean ideaActive) {
		var stream = summary.stages().stream()
			.map(stage -> stage.stage() + ":" + stage.status() + "(" + stage.durationMillis() + "ms, attrs=" + stage.attributes() + ")");
		if (ideaActive) {
			return stream.collect(Collectors.joining(System.lineSeparator() + "  ", "  ", ""));
		}
		return stream.collect(Collectors.joining(", "));
	}

	private String formatOutputs(DialoguePipelineTracker.PipelineSummary summary, boolean ideaActive) {
		if (summary.llmOutputs().isEmpty()) {
			return "(none)";
		}
		var stream = summary.llmOutputs().stream();
		if (ideaActive) {
			return stream.collect(Collectors.joining(System.lineSeparator() + "  - ", "  - ", ""));
		}
		return stream.collect(Collectors.joining(" | "));
	}
}
