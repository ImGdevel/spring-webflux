package com.study.webflux.rag.application.monitoring;

public enum DialoguePipelineStage {
	QUERY_PERSISTENCE,
	MEMORY_RETRIEVAL,
	RETRIEVAL,
	PROMPT_BUILDING,
	LLM_COMPLETION,
	SENTENCE_ASSEMBLY,
	TTS_PREPARATION,
	TTS_SYNTHESIS
}
