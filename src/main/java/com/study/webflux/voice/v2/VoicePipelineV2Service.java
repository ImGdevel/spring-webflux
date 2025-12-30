package com.study.webflux.voice.v2;

import java.time.Duration;
import java.util.Base64;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

/**
 * v2 음성 파이프라인의 핵심 오케스트레이션을 담당하는 서비스.
 *
 * <p>
 * 1. LLM 스트리밍 API 로부터 텍스트 조각 스트림을 받고<br>
 * 2. 문장 단위로 버퍼링한 다음<br>
 * 3. 각 문장을 TTS 스트리밍 API 에 전달해 오디오 스트림을 생성한다.<br>
 * 4. 구성 설정(`voice.v2.*`)을 활용해 타임아웃/재시도/청크 정책을 적용한다.
 * </p>
 */
@Slf4j
@Service
public class VoicePipelineV2Service {

	private static final Scheduler BLOCKING_SCHEDULER = Schedulers.boundedElastic();

	private final LlmStreamingClient llmStreamingClient;
	private final TtsStreamingClient ttsStreamingClient;
	private final SentenceAssemblyService sentenceAssemblyService;
	private final AudioChunkingService audioChunkingService;
	private final Duration llmTimeout;
	private final Duration ttsTimeout;
	private final int llmRetryAttempts;

	public VoicePipelineV2Service(
		LlmStreamingClient llmStreamingClient,
		TtsStreamingClient ttsStreamingClient,
		SentenceAssemblyService sentenceAssemblyService,
		AudioChunkingService audioChunkingService,
		VoiceV2Properties properties
	) {
		this.llmStreamingClient = llmStreamingClient;
		this.ttsStreamingClient = ttsStreamingClient;
		this.sentenceAssemblyService = sentenceAssemblyService;
		this.audioChunkingService = audioChunkingService;
		this.llmTimeout = properties.getLlmTimeout();
		this.ttsTimeout = properties.getTtsTimeout();
		this.llmRetryAttempts = Math.max(0, properties.getLlmRetryAttempts());
	}

	/**
	 * v2 파이프라인 전체를 실행한다.
	 *
	 * @param request 클라이언트 요청 DTO
	 * @return LLM → TTS 를 거친 바이너리 오디오 청크 스트림
	 */
	public Flux<byte[]> runPipeline(VoiceV2Request request) {
		return assemblePipeline(request, audioChunkingService::chunk);
	}

	/**
	 * chunk 크기를 조절할 수 있는 파이프라인 실행.
	 *
	 * @param request 클라이언트 요청 DTO
	 * @param preferredChunkSize 바이트 단위 chunk 크기 (0 이하이면 chunking 생략)
	 * @return 오디오 청크 스트림
	 */
	public Flux<byte[]> runPipeline(VoiceV2Request request, int preferredChunkSize) {
		return assemblePipeline(request, flux -> audioChunkingService.chunk(flux, preferredChunkSize));
	}

	private Flux<byte[]> assemblePipeline(VoiceV2Request request, java.util.function.Function<Flux<byte[]>, Flux<byte[]>> chunker) {
		Flux<String> llmStream = llmStreamingClient.streamCompletion(request.text())
			.subscribeOn(BLOCKING_SCHEDULER)
			.timeout(llmTimeout)
			.transform(this::applyLlmRetry)
			.onErrorMap(error -> new VoicePipelineException("LLM 스트림 처리 중 오류가 발생했습니다.", error))
			.doOnSubscribe(sub -> log.info("LLM stream subscribed textLength={} requestedAt={}", request.text().length(), request.requestedAt()))
			.doOnCancel(() -> log.warn("LLM stream cancelled textLength={}", request.text().length()));

		Flux<String> sentenceStream = sentenceAssemblyService.assemble(llmStream)
			.doOnNext(sentence -> log.debug("Sentence assembled: {}", sentence))
			.doOnError(error -> log.error("Sentence assembly failed", error));

		Flux<byte[]> rawAudioStream = sentenceStream
			.publishOn(BLOCKING_SCHEDULER)
			.concatMap(sentence -> ttsStreamingClient.streamAudio(sentence)
				.timeout(ttsTimeout)
				.onErrorMap(error -> new VoicePipelineException("TTS 스트림 처리 중 오류가 발생했습니다.", error))
				.doOnSubscribe(sub -> log.info("TTS stream subscribed sentenceLength={}", sentence.length()))
				.doOnCancel(() -> log.warn("TTS stream cancelled sentenceLength={}", sentence.length()))
			);

		Flux<byte[]> chunked = chunker.apply(rawAudioStream)
			.doOnSubscribe(sub -> log.info("Audio chunking subscribed for request at {}", request.requestedAt()))
			.doOnCancel(() -> log.warn("Audio chunking cancelled for request at {}", request.requestedAt()));

		return chunked;
	}

	private Flux<String> applyLlmRetry(Flux<String> source) {
		if (llmRetryAttempts <= 0) {
			return source;
		}
		return source.retryWhen(Retry.fixedDelay(llmRetryAttempts, Duration.ofMillis(200)));
	}

	/**
	 * Base64 문자열 스트림으로 변환한 파이프라인 실행.
	 *
	 * @param request 클라이언트 요청 DTO
	 * @return SSE 로 바로 사용할 수 있는 Base64 문자열 스트림
	 */
	public Flux<String> runPipelineBase64(VoiceV2Request request) {
		return runPipeline(request)
			.doOnSubscribe(sub -> log.info("Base64 encode subscribe requestAt={}", request.requestedAt()))
			.map(bytes -> Base64.getEncoder().encodeToString(bytes));
	}

	/**
	 * 테스트나 특수 케이스에서 chunkSize 를 오버라이드하고 싶을 때 사용할 수 있는 변형.
	 *
	 * @param request 클라이언트 요청 DTO
	 * @param chunkSize 오버라이드할 청크 크기
	 * @return Base64 문자열 스트림
	 */
	public Flux<String> runPipelineBase64(VoiceV2Request request, int chunkSize) {
		return runPipeline(request, chunkSize)
			.map(bytes -> Base64.getEncoder().encodeToString(bytes));
	}
}

