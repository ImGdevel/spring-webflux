package com.study.webflux.voice.v2;

import java.util.Base64;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * v2 음성 파이프라인의 핵심 오케스트레이션을 담당하는 서비스.
 *
 * <p>
 * 1. LLM 스트리밍 API 로부터 텍스트 조각 스트림을 받고<br>
 * 2. 문장 단위로 버퍼링한 다음<br>
 * 3. 각 문장을 TTS 스트리밍 API 에 전달해 오디오 스트림을 생성한다.
 * 4. `Schedulers.boundedElastic()` 을 통해 블로킹 I/O 가능성을 분리한다.
 * </p>
 */
@Service
public class VoicePipelineV2Service {

	private static final Scheduler BLOCKING_SCHEDULER = Schedulers.boundedElastic();

	private final LlmStreamingClient llmStreamingClient;
	private final TtsStreamingClient ttsStreamingClient;
	private final SentenceAssemblyService sentenceAssemblyService;

	public VoicePipelineV2Service(
		LlmStreamingClient llmStreamingClient,
		TtsStreamingClient ttsStreamingClient,
		SentenceAssemblyService sentenceAssemblyService
	) {
		this.llmStreamingClient = llmStreamingClient;
		this.ttsStreamingClient = ttsStreamingClient;
		this.sentenceAssemblyService = sentenceAssemblyService;
	}

	/**
	 * v2 파이프라인 전체를 실행한다.
	 *
	 * @param request 클라이언트 요청 DTO
	 * @return LLM → TTS 를 거친 바이너리 오디오 청크 스트림
	 */
	public Flux<byte[]> runPipeline(VoiceV2Request request) {
		Flux<String> llmStream = llmStreamingClient.streamCompletion(request.text())
			.subscribeOn(BLOCKING_SCHEDULER);

		Flux<String> sentenceStream = sentenceAssemblyService.assemble(llmStream);

		return sentenceStream
			.publishOn(BLOCKING_SCHEDULER)
			.concatMap(ttsStreamingClient::streamAudio);
	}

	/**
	 * Base64 문자열 스트림으로 변환한 파이프라인 실행.
	 *
	 * @param request 클라이언트 요청 DTO
	 * @return SSE 로 바로 사용할 수 있는 Base64 문자열 스트림
	 */
	public Flux<String> runPipelineBase64(VoiceV2Request request) {
		return runPipeline(request)
			.map(bytes -> Base64.getEncoder().encodeToString(bytes));
	}
}
