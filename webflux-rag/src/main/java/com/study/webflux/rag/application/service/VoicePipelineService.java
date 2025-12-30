package com.study.webflux.rag.application.service;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.study.webflux.rag.application.monitoring.VoicePipelineMonitor;
import com.study.webflux.rag.application.monitoring.VoicePipelineStage;
import com.study.webflux.rag.application.monitoring.VoicePipelineTracker;
import com.study.webflux.rag.domain.model.llm.CompletionRequest;
import com.study.webflux.rag.domain.model.conversation.ConversationContext;
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

	private static final Logger log = LoggerFactory.getLogger(VoicePipelineService.class);

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

		Mono<Void> ttsWarmup = tracker.traceMono(
				VoicePipelineStage.TTS_PREPARATION,
				() -> ttsPort.prepare()
					.doOnError(error -> log.warn("Pipeline {} TTS warmup failed", tracker.pipelineId(), error))
					.onErrorResume(error -> Mono.empty())
			)
			.cache();

		ttsWarmup.subscribe();

		Mono<ConversationTurn> queryTurn = tracker.traceMono(VoicePipelineStage.QUERY_PERSISTENCE, () -> saveQuery(text));

		Mono<RetrievalContext> retrievalContext = queryTurn
			.flatMap(turn -> tracker.traceMono(VoicePipelineStage.RETRIEVAL, () -> retrievalPort.retrieve(text, 3)))
			.doOnNext(context -> tracker.recordStageAttribute(
				VoicePipelineStage.RETRIEVAL,
				"documentCount",
				context.documentCount()
			));

		Flux<String> llmTokens = Mono.zip(retrievalContext, loadConversationHistory())
			.flatMapMany(tuple -> {
				RetrievalContext context = tuple.getT1();
				ConversationContext conversationContext = tuple.getT2();

				return tracker.traceMono(
					VoicePipelineStage.PROMPT_BUILDING,
					() -> Mono.fromCallable(() -> promptTemplate.buildPromptWithConversation(context, conversationContext))
				).flatMapMany(prompt -> {
					CompletionRequest request = CompletionRequest.streaming(prompt, "gpt-3.5-turbo");
					tracker.recordStageAttribute(VoicePipelineStage.LLM_COMPLETION, "model", request.model());
					return tracker.traceFlux(VoicePipelineStage.LLM_COMPLETION, () -> llmPort.streamCompletion(request));
				});
			})
			.subscribeOn(Schedulers.boundedElastic())
			.doOnNext(token -> tracker.incrementStageCounter(VoicePipelineStage.LLM_COMPLETION, "tokenCount", 1));

		Flux<String> sentences = tracker.traceFlux(
				VoicePipelineStage.SENTENCE_ASSEMBLY,
				() -> sentenceAssembler.assemble(llmTokens)
			)
			.cache()
			.doOnNext(sentence -> {
				tracker.incrementStageCounter(VoicePipelineStage.SENTENCE_ASSEMBLY, "sentenceCount", 1);
				tracker.recordLlmOutput(sentence);
			});

		StringBuilder responseBuilder = new StringBuilder();
		sentences.subscribe(sentence -> responseBuilder.append(sentence).append(" "));

		Mono<Void> saveResponse = queryTurn
			.delayUntil(turn -> sentences.then())
			.flatMap(turn -> {
				String fullResponse = responseBuilder.toString().trim();
				if (!fullResponse.isEmpty()) {
					return conversationRepository.save(turn.withResponse(fullResponse));
				}
				return Mono.empty();
			})
			.then();

		saveResponse.subscribe();

		Flux<byte[]> audioFlux = sentences.publish(sharedSentences -> {
			Mono<String> firstSentenceMono = sharedSentences.take(1).singleOrEmpty().cache();
			Flux<String> remainingSentences = sharedSentences.skip(1);

			Flux<byte[]> firstSentenceAudio = firstSentenceMono
				.flatMapMany(sentence ->
					ttsWarmup.thenMany(ttsPort.streamSynthesize(sentence))
				)
				.publishOn(Schedulers.boundedElastic());

			Flux<byte[]> remainingAudio = remainingSentences
				.publishOn(Schedulers.boundedElastic())
				.concatMap(sentence -> ttsWarmup.thenMany(ttsPort.streamSynthesize(sentence)));

			return Flux.mergeSequential(firstSentenceAudio, remainingAudio);
		});

		Flux<byte[]> audioStream = tracker.traceFlux(
				VoicePipelineStage.TTS_SYNTHESIS,
				() -> audioFlux
			)
			.doOnNext(chunk -> {
				tracker.incrementStageCounter(VoicePipelineStage.TTS_SYNTHESIS, "audioChunks", 1);
				tracker.markResponseEmission();
			});

		return tracker.attachLifecycle(audioStream);
	}

	private Mono<ConversationTurn> saveQuery(String text) {
		ConversationTurn turn = ConversationTurn.create(text);
		return conversationRepository.save(turn);
	}

	private Mono<ConversationContext> loadConversationHistory() {
		return conversationRepository.findRecent(10)
			.collectList()
			.map(ConversationContext::of)
			.defaultIfEmpty(ConversationContext.empty());
	}
}
