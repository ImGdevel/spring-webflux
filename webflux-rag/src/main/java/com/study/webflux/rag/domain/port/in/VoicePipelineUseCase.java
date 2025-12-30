package com.study.webflux.rag.domain.port.in;

import reactor.core.publisher.Flux;

public interface VoicePipelineUseCase {
	Flux<String> executeStreaming(String text);

	Flux<byte[]> executeAudioStreaming(String text);
}
