package com.study.webflux.rag.application.service;

import java.util.Base64;

import org.springframework.stereotype.Service;

import com.study.webflux.rag.domain.model.conversation.ConversationTurn;
import com.study.webflux.rag.domain.model.llm.CompletionRequest;
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

	public VoicePipelineService(
		LlmPort llmPort,
		TtsPort ttsPort,
		RetrievalPort retrievalPort,
		ConversationRepository conversationRepository,
		PromptTemplatePort promptTemplate,
		SentenceAssembler sentenceAssembler) {
		this.llmPort = llmPort;
		this.ttsPort = ttsPort;
		this.retrievalPort = retrievalPort;
		this.conversationRepository = conversationRepository;
		this.promptTemplate = promptTemplate;
		this.sentenceAssembler = sentenceAssembler;
	}

	@Override
	public Flux<String> executeStreaming(String text) {
		return executeAudioStreaming(text)
			.map(bytes -> Base64.getEncoder().encodeToString(bytes));
	}

	@Override
	public Flux<byte[]> executeAudioStreaming(String text) {
		return saveQuery(text)
			.flatMap(turn -> retrievalPort.retrieve(text, 3))
			.flatMapMany(context -> {
				String prompt = promptTemplate.buildPrompt(context);
				CompletionRequest request = CompletionRequest.streaming(prompt, "gpt-3.5-turbo");
				return llmPort.streamCompletion(request);
			})
			.subscribeOn(Schedulers.boundedElastic())
			.transform(sentenceAssembler::assemble)
			.publishOn(Schedulers.boundedElastic())
			.concatMap(sentence -> ttsPort.streamSynthesize(sentence));
	}

	private Mono<ConversationTurn> saveQuery(String text) {
		ConversationTurn turn = ConversationTurn.create(text);
		return conversationRepository.save(turn);
	}
}
