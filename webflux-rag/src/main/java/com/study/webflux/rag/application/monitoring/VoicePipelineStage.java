package com.study.webflux.rag.application.monitoring;

public enum VoicePipelineStage {
	QUERY_PERSISTENCE,
	RETRIEVAL,
	PROMPT_BUILDING,
	LLM_COMPLETION,
	SENTENCE_ASSEMBLY,
	TTS_SYNTHESIS
}
