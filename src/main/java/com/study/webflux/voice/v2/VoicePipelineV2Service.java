package com.study.webflux.voice.v2;

import java.util.Base64;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

/**
 * v2 음성 파이프라인의 핵심 오케스트레이션을 담당하는 서비스.
 *
 * <p>
 * 1. LLM 스트리밍 API 로부터 텍스트 조각 스트림을 받고<br>
 * 2. 문장 단위로 버퍼링한 다음<br>
 * 3. 각 문장을 TTS 스트리밍 API 에 전달해 오디오 스트림을 생성한다.<br>
 * 4. 구성 설정(`voice.v2.chunk-size`)에 따라 TTS 바이너리를 지정한 크기로 다시 묶어서 클라이언트에 전달한다.
 * </p>
 */
@Service
public class VoicePipelineV2Service {

	private final LlmStreamingClient llmStreamingClient;
	private final TtsStreamingClient ttsStreamingClient;
	private final SentenceAssemblyService sentenceAssemblyService;
	private final AudioChunkingService audioChunkingService;

	public VoicePipelineV2Service(
		LlmStreamingClient llmStreamingClient,
		TtsStreamingClient ttsStreamingClient,
		SentenceAssemblyService sentenceAssemblyService,
		AudioChunkingService audioChunkingService
	) {
		this.llmStreamingClient = llmStreamingClient;
		this.ttsStreamingClient = ttsStreamingClient;
		this.sentenceAssemblyService = sentenceAssemblyService;
		this.audioChunkingService = audioChunkingService;
	}

	/**
	 * v2 파이프라인 전체를 실행한다.
	 *
	 * @param request 클라이언트 요청 DTO
	 * @return LLM → TTS 를 거친 바이너리 오디오 청크 스트림
	 */
	public Flux<byte[]> runPipeline(VoiceV2Request request) {
		Flux<String> llmStream = llmStreamingClient.streamCompletion(request.text());
		Flux<String> sentenceStream = sentenceAssemblyService.assemble(llmStream);
		Flux<byte[]> rawAudioStream = sentenceStream.concatMap(ttsStreamingClient::streamAudio);
		return audioChunkingService.chunk(rawAudioStream);
	}

	/**
	 * chunk 크기를 조절할 수 있는 파이프라인 실행.
	 *
	 * @param request 클라이언트 요청 DTO
	 * @param preferredChunkSize 바이트 단위 chunk 크기 (0 이하이면 chunking 생략)
	 * @return 오디오 청크 스트림
	 */
	public Flux<byte[]> runPipeline(VoiceV2Request request, int preferredChunkSize) {
		Flux<String> llmStream = llmStreamingClient.streamCompletion(request.text());
		Flux<String> sentenceStream = sentenceAssemblyService.assemble(llmStream);
		Flux<byte[]> rawAudioStream = sentenceStream.concatMap(ttsStreamingClient::streamAudio);
		return audioChunkingService.chunk(rawAudioStream, preferredChunkSize);
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
