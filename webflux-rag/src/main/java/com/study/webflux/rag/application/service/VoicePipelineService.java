package com.study.webflux.rag.application.service;

import java.util.Base64;

import org.springframework.stereotype.Service;

import com.study.webflux.rag.application.monitoring.VoicePipelineMonitor;
import com.study.webflux.rag.application.monitoring.VoicePipelineStage;
import com.study.webflux.rag.application.monitoring.VoicePipelineTracker;
import com.study.webflux.rag.domain.model.llm.CompletionRequest;
import com.study.webflux.rag.domain.model.conversation.ConversationTurn;
import com.study.webflux.rag.domain.model.rag.RetrievalContext;
import com.study.webflux.rag.domain.port.in.VoicePipelineUseCase;
import com.study.webflux.rag.domain.port.out.ConversationRepository;
import com.study.webflux.rag.domain.port.out.LlmPort;
import com.study.webflux.rag.domain.port.out.PromptTemplatePort;
import com.study.webflux.rag.domain.port.out.RetrievalPort;
import com.study.webflux.rag.domain.port.out.TtsPort;
import com.study.webflux.rag.domain.service.SentenceAssembler;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class VoicePipelineService implements VoicePipelineUseCase {

	private final LlmPort llmPort;
	private final TtsPort ttsPort;
	private final RetrievalPort retrievalPort;
	private final ConversationRepository conversationRepository;
	private final PromptTemplatePort promptTemplate;
	private final SentenceAssembler sentenceAssembler;
	private final VoicePipelineMonitor pipelineMonitor;

	public VoicePipelineService(
		LlmPort llmPort,
		TtsPort ttsPort,
		RetrievalPort retrievalPort,
		ConversationRepository conversationRepository,
		PromptTemplatePort promptTemplate,
		SentenceAssembler sentenceAssembler,
		VoicePipelineMonitor pipelineMonitor) {
		this.llmPort = llmPort;
		this.ttsPort = ttsPort;
		this.retrievalPort = retrievalPort;
		this.conversationRepository = conversationRepository;
		this.promptTemplate = promptTemplate;
		this.sentenceAssembler = sentenceAssembler;
		this.pipelineMonitor = pipelineMonitor;
	}

	@Override
	public Flux<String> executeStreaming(String text) {
		return executeAudioStreaming(text)
			.map(bytes -> Base64.getEncoder().encodeToString(bytes));
	}

	@Override
	public Flux<byte[]> executeAudioStreaming(String text) {
		VoicePipelineTracker tracker = pipelineMonitor.create(text);

		Mono<RetrievalContext> retrievalContext = tracker.traceMono(VoicePipelineStage.QUERY_PERSISTENCE, () -> saveQuery(text))
			.flatMap(turn -> tracker.traceMono(VoicePipelineStage.RETRIEVAL, () -> retrievalPort.retrieve(text, 3)))
			.doOnNext(context -> tracker.recordStageAttribute(
				VoicePipelineStage.RETRIEVAL,
				"documentCount",
				context.documentCount()
			));

		Flux<String> llmTokens = retrievalContext.flatMapMany(context ->
				tracker.traceMono(
					VoicePipelineStage.PROMPT_BUILDING,
					() -> Mono.fromCallable(() -> promptTemplate.buildPrompt(context))
				).flatMapMany(prompt -> {
					CompletionRequest request = CompletionRequest.streaming(prompt, "gpt-3.5-turbo");
					tracker.recordStageAttribute(VoicePipelineStage.LLM_COMPLETION, "model", request.model());
					return tracker.traceFlux(VoicePipelineStage.LLM_COMPLETION, () -> llmPort.streamCompletion(request));
				})
			)
			.subscribeOn(Schedulers.boundedElastic())
			.doOnNext(token -> tracker.incrementStageCounter(VoicePipelineStage.LLM_COMPLETION, "tokenCount", 1));

		Flux<String> sentences = tracker.traceFlux(
				VoicePipelineStage.SENTENCE_ASSEMBLY,
				() -> sentenceAssembler.assemble(llmTokens)
			)
			.doOnNext(sentence -> tracker.incrementStageCounter(VoicePipelineStage.SENTENCE_ASSEMBLY, "sentenceCount", 1));

		Flux<byte[]> audioStream = tracker.traceFlux(
				VoicePipelineStage.TTS_SYNTHESIS,
				() -> sentences
					.publishOn(Schedulers.boundedElastic())
					.concatMap(sentence -> ttsPort.streamSynthesize(sentence))
			)
			.doOnNext(chunk -> tracker.incrementStageCounter(VoicePipelineStage.TTS_SYNTHESIS, "audioChunks", 1));

		return tracker.attachLifecycle(audioStream);
	}

	private Mono<ConversationTurn> saveQuery(String text) {
		ConversationTurn turn = ConversationTurn.create(text);
		return conversationRepository.save(turn);
	}
}
