package com.study.webflux.voice.service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.study.webflux.voice.client.LlmStreamingClient;
import com.study.webflux.voice.client.TtsStreamingClient;
import com.study.webflux.voice.model.RagVoiceRequest;
import com.study.webflux.voice.model.RetrievalResult;
import com.study.webflux.voice.repository.ConversationHistoryRepository;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Service
public class RagVoicePipelineService {

	private static final Scheduler BLOCKING_SCHEDULER = Schedulers.boundedElastic();

	private final ConversationHistoryRepository repository;
	private final FakeRagRetrievalService retrievalService;
	private final LlmStreamingClient llmClient;
	private final TtsStreamingClient ttsClient;
	private final SentenceAssemblyService sentenceAssembly;

	public RagVoicePipelineService(
		ConversationHistoryRepository repository,
		FakeRagRetrievalService retrievalService,
		LlmStreamingClient llmClient,
		TtsStreamingClient ttsClient,
		SentenceAssemblyService sentenceAssembly
	) {
		this.repository = repository;
		this.retrievalService = retrievalService;
		this.llmClient = llmClient;
		this.ttsClient = ttsClient;
		this.sentenceAssembly = sentenceAssembly;
	}

	public Flux<String> runPipeline(RagVoiceRequest request) {
		return repository.saveQuery(request.text())
			.flatMap(saved -> retrievalService.retrieve(request.text(), 3))
			.map(this::buildAugmentedPrompt)
			.flatMapMany(prompt -> llmClient.streamCompletion(prompt))
			.subscribeOn(BLOCKING_SCHEDULER)
			.transform(sentenceAssembly::assemble)
			.publishOn(BLOCKING_SCHEDULER)
			.concatMap(ttsClient::streamAudio)
			.map(bytes -> Base64.getEncoder().encodeToString(bytes));
	}

	public Flux<byte[]> runPipelineAudio(RagVoiceRequest request) {
		return repository.saveQuery(request.text())
			.flatMap(saved -> retrievalService.retrieve(request.text(), 3))
			.map(this::buildAugmentedPrompt)
			.flatMapMany(prompt -> llmClient.streamCompletion(prompt))
			.subscribeOn(BLOCKING_SCHEDULER)
			.transform(sentenceAssembly::assemble)
			.publishOn(BLOCKING_SCHEDULER)
			.concatMap(ttsClient::streamAudio);
	}

	private String buildAugmentedPrompt(List<RetrievalResult> results) {
		if (results.isEmpty()) {
			return "사용자의 질문에 자연스럽게 대답하세요. 응답은 순수한 대화 텍스트만 포함하고, 불릿 포인트(-, *, 숫자 등)나 마크다운 형식을 사용하지 마세요.";
		}

		String context = results.stream()
			.map(result -> result.message().query())
			.collect(Collectors.joining("\n"));

		return "이전 대화 맥락:\n" + context + "\n\n위 맥락을 참고하여 사용자의 질문에 자연스럽게 대답하세요. 응답은 순수한 대화 텍스트만 포함하고, 불릿 포인트(-, *, 숫자 등)나 마크다운 형식을 사용하지 마세요.";
	}
}
