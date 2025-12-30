package com.study.webflux.rag.application.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.study.webflux.rag.application.monitoring.DialoguePipelineMonitor;
import com.study.webflux.rag.application.monitoring.DialoguePipelineStage;
import com.study.webflux.rag.application.monitoring.DialoguePipelineTracker;
import com.study.webflux.rag.domain.model.conversation.ConversationContext;
import com.study.webflux.rag.domain.model.conversation.ConversationTurn;
import com.study.webflux.rag.domain.model.llm.CompletionRequest;
import com.study.webflux.rag.domain.model.llm.Message;
import com.study.webflux.rag.domain.model.rag.RetrievalContext;
import com.study.webflux.rag.domain.port.in.DialoguePipelineUseCase;
import com.study.webflux.rag.domain.port.out.ConversationRepository;
import com.study.webflux.rag.domain.port.out.LlmPort;
import com.study.webflux.rag.domain.port.out.RetrievalPort;
import com.study.webflux.rag.domain.port.out.TtsPort;
import com.study.webflux.rag.domain.service.SentenceAssembler;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class DialoguePipelineService implements DialoguePipelineUseCase {

	private static final Logger log = LoggerFactory.getLogger(DialoguePipelineService.class);

	private final LlmPort llmPort;
	private final TtsPort ttsPort;
	private final RetrievalPort retrievalPort;
	private final ConversationRepository conversationRepository;
	private final SentenceAssembler sentenceAssembler;
	private final DialoguePipelineMonitor pipelineMonitor;

	public DialoguePipelineService(
		LlmPort llmPort,
		TtsPort ttsPort,
		RetrievalPort retrievalPort,
		ConversationRepository conversationRepository,
		SentenceAssembler sentenceAssembler,
		DialoguePipelineMonitor pipelineMonitor) {
		this.llmPort = llmPort;
		this.ttsPort = ttsPort;
		this.retrievalPort = retrievalPort;
		this.conversationRepository = conversationRepository;
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
		DialoguePipelineTracker tracker = pipelineMonitor.create(text);

		Mono<Void> ttsWarmup = tracker.traceMono(
				DialoguePipelineStage.TTS_PREPARATION,
				() -> ttsPort.prepare()
					.doOnError(error -> log.warn("Pipeline {} TTS warmup failed", tracker.pipelineId(), error))
					.onErrorResume(error -> Mono.empty())
			)
			.cache();

		ttsWarmup.subscribe();

		Mono<ConversationTurn> queryTurn = tracker.traceMono(DialoguePipelineStage.QUERY_PERSISTENCE, () -> saveQuery(text));

		Mono<RetrievalContext> retrievalContext = queryTurn
			.flatMap(turn -> tracker.traceMono(DialoguePipelineStage.RETRIEVAL, () -> retrievalPort.retrieve(text, 3)))
			.doOnNext(context -> tracker.recordStageAttribute(
				DialoguePipelineStage.RETRIEVAL,
				"documentCount",
				context.documentCount()
			));

		Flux<String> llmTokens = Mono.zip(retrievalContext, loadConversationHistory(), queryTurn)
			.flatMapMany(tuple -> {
				RetrievalContext context = tuple.getT1();
				ConversationContext conversationContext = tuple.getT2();
				ConversationTurn currentTurn = tuple.getT3();

				return tracker.traceMono(
					DialoguePipelineStage.PROMPT_BUILDING,
					() -> Mono.fromCallable(() -> buildMessages(context, conversationContext, currentTurn.query()))
				).flatMapMany(messages -> {
					CompletionRequest request = CompletionRequest.withMessages(messages, "gpt-4o-mini", true);
					tracker.recordStageAttribute(DialoguePipelineStage.LLM_COMPLETION, "model", request.model());
					return tracker.traceFlux(DialoguePipelineStage.LLM_COMPLETION, () -> llmPort.streamCompletion(request));
				});
			})
			.subscribeOn(Schedulers.boundedElastic())
			.doOnNext(token -> tracker.incrementStageCounter(DialoguePipelineStage.LLM_COMPLETION, "tokenCount", 1));

		Flux<String> sentences = tracker.traceFlux(
				DialoguePipelineStage.SENTENCE_ASSEMBLY,
				() -> sentenceAssembler.assemble(llmTokens)
			)
			.doOnNext(sentence -> {
				tracker.incrementStageCounter(DialoguePipelineStage.SENTENCE_ASSEMBLY, "sentenceCount", 1);
				tracker.recordLlmOutput(sentence);
			})
			.share();

		Flux<byte[]> audioFlux = sentences.publish(sharedSentences -> {
			Flux<String> cachedSentences = sharedSentences.cache();

			cachedSentences.collectList()
				.flatMap(sentenceList -> {
					String fullResponse = String.join(" ", sentenceList);
					return queryTurn.flatMap(turn ->
						conversationRepository.save(turn.withResponse(fullResponse))
					);
				})
				.subscribe();

			Mono<String> firstSentenceMono = cachedSentences.take(1).singleOrEmpty().cache();
			Flux<String> remainingSentences = cachedSentences.skip(1);

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
				DialoguePipelineStage.TTS_SYNTHESIS,
				() -> audioFlux
			)
			.doOnNext(chunk -> {
				tracker.incrementStageCounter(DialoguePipelineStage.TTS_SYNTHESIS, "audioChunks", 1);
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

	private List<Message> buildMessages(RetrievalContext context, ConversationContext conversationContext, String currentQuery) {
		List<Message> messages = new ArrayList<>();

		String systemPrompt = buildSystemPrompt(context);
		messages.add(Message.system(systemPrompt));

		conversationContext.turns().stream()
			.filter(turn -> turn.response() != null)
			.forEach(turn -> {
				messages.add(Message.user(turn.query()));
				messages.add(Message.assistant(turn.response()));
			});

		messages.add(Message.user(currentQuery));

		return messages;
	}

	private String buildSystemPrompt(RetrievalContext context) {
		if (context.isEmpty()) {
			return "자연스럽게 대화하세요. 과도한 존댓말이나 '도와드리겠습니다' 같은 틀에 박힌 표현은 피하세요.";
		}

		String contextText = context.documents().stream()
			.map(doc -> doc.content())
			.collect(Collectors.joining("\n"));

		return String.format(
			"다음 정보를 참고해서 답변하세요:\n\n%s\n\n자연스럽게 대화하세요. 필요한 정보만 간결하게 답변하고, 불필요한 인사말이나 '도와드리겠습니다' 같은 표현은 생략하세요.",
			contextText
		);
	}
}
