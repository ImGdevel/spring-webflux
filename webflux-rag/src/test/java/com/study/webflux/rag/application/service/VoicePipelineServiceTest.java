package com.study.webflux.rag.application.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.study.webflux.rag.application.monitoring.VoicePipelineMonitor;
import com.study.webflux.rag.domain.model.conversation.ConversationTurn;
import com.study.webflux.rag.domain.model.llm.CompletionRequest;
import com.study.webflux.rag.domain.model.rag.RetrievalContext;
import com.study.webflux.rag.domain.model.rag.RetrievalDocument;
import com.study.webflux.rag.domain.port.out.ConversationRepository;
import com.study.webflux.rag.domain.port.out.LlmPort;
import com.study.webflux.rag.domain.port.out.PromptTemplatePort;
import com.study.webflux.rag.domain.port.out.RetrievalPort;
import com.study.webflux.rag.domain.port.out.TtsPort;
import com.study.webflux.rag.domain.service.SentenceAssembler;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class VoicePipelineServiceTest {

	@Mock
	private LlmPort llmPort;

	@Mock
	private TtsPort ttsPort;

	@Mock
	private RetrievalPort retrievalPort;

	@Mock
	private ConversationRepository conversationRepository;

	@Mock
	private PromptTemplatePort promptTemplate;

	private SentenceAssembler sentenceAssembler;

	private VoicePipelineService service;
	private VoicePipelineMonitor pipelineMonitor;

	@BeforeEach
	void setUp() {
		sentenceAssembler = new SentenceAssembler();
		pipelineMonitor = new VoicePipelineMonitor(summary -> {});
		service = new VoicePipelineService(
			llmPort,
			ttsPort,
			retrievalPort,
			conversationRepository,
			promptTemplate,
			sentenceAssembler,
			pipelineMonitor
		);
	}

	@Test
	void executeStreaming_shouldReturnBase64EncodedAudio() {
		String testText = "Hello";
		byte[] audioBytes = "audio-data".getBytes();
		String expectedBase64 = Base64.getEncoder().encodeToString(audioBytes);

		ConversationTurn turn = ConversationTurn.create(testText);
		RetrievalContext emptyContext = RetrievalContext.empty(testText);

		when(conversationRepository.save(any(ConversationTurn.class)))
			.thenReturn(Mono.just(turn));
		when(retrievalPort.retrieve(eq(testText), eq(3)))
			.thenReturn(Mono.just(emptyContext));
		when(promptTemplate.buildPrompt(any(RetrievalContext.class)))
			.thenReturn("Test prompt");
		when(llmPort.streamCompletion(any(CompletionRequest.class)))
			.thenReturn(Flux.just("Hello", " world", "."));
		when(ttsPort.streamSynthesize(anyString()))
			.thenReturn(Flux.just(audioBytes));

		StepVerifier.create(service.executeStreaming(testText))
			.expectNext(expectedBase64)
			.verifyComplete();

		verify(conversationRepository).save(any(ConversationTurn.class));
		verify(retrievalPort).retrieve(testText, 3);
		verify(promptTemplate).buildPrompt(any(RetrievalContext.class));
		verify(llmPort).streamCompletion(any(CompletionRequest.class));
		verify(ttsPort).streamSynthesize("Hello world.");
	}

	@Test
	void executeAudioStreaming_shouldReturnRawAudioBytes() {
		String testText = "Test query";
		byte[] audioBytes1 = "audio1".getBytes();
		byte[] audioBytes2 = "audio2".getBytes();

		ConversationTurn turn = ConversationTurn.create(testText);
		RetrievalDocument doc = RetrievalDocument.of("Previous context", 5);
		RetrievalContext context = RetrievalContext.of(testText, List.of(doc));

		when(conversationRepository.save(any(ConversationTurn.class)))
			.thenReturn(Mono.just(turn));
		when(retrievalPort.retrieve(eq(testText), eq(3)))
			.thenReturn(Mono.just(context));
		when(promptTemplate.buildPrompt(any(RetrievalContext.class)))
			.thenReturn("Augmented prompt");
		when(llmPort.streamCompletion(any(CompletionRequest.class)))
			.thenReturn(Flux.just("First", " sentence", ".", " Second", " sentence", "."));
		when(ttsPort.streamSynthesize("First sentence."))
			.thenReturn(Flux.just(audioBytes1));
		when(ttsPort.streamSynthesize("Second sentence."))
			.thenReturn(Flux.just(audioBytes2));

		StepVerifier.create(service.executeAudioStreaming(testText))
			.expectNext(audioBytes1)
			.expectNext(audioBytes2)
			.verifyComplete();

		verify(ttsPort).streamSynthesize("First sentence.");
		verify(ttsPort).streamSynthesize("Second sentence.");
	}

	@Test
	void executeAudioStreaming_withEmptyContext_shouldUseDefaultPrompt() {
		String testText = "Query without context";
		byte[] audioBytes = "audio".getBytes();

		ConversationTurn turn = ConversationTurn.create(testText);
		RetrievalContext emptyContext = RetrievalContext.empty(testText);

		when(conversationRepository.save(any(ConversationTurn.class)))
			.thenReturn(Mono.just(turn));
		when(retrievalPort.retrieve(eq(testText), eq(3)))
			.thenReturn(Mono.just(emptyContext));
		when(promptTemplate.buildPrompt(any(RetrievalContext.class)))
			.thenReturn("Default prompt");
		when(llmPort.streamCompletion(any(CompletionRequest.class)))
			.thenReturn(Flux.just("Response", "."));
		when(ttsPort.streamSynthesize(anyString()))
			.thenReturn(Flux.just(audioBytes));

		StepVerifier.create(service.executeAudioStreaming(testText))
			.expectNext(audioBytes)
			.verifyComplete();

		verify(promptTemplate).buildPrompt(emptyContext);
	}

	@Test
	void executeAudioStreaming_shouldHandleMultipleSentences() {
		String testText = "Multi sentence test";
		byte[] audio1 = "a1".getBytes();
		byte[] audio2 = "a2".getBytes();
		byte[] audio3 = "a3".getBytes();

		ConversationTurn turn = ConversationTurn.create(testText);
		RetrievalContext emptyContext = RetrievalContext.empty(testText);

		when(conversationRepository.save(any(ConversationTurn.class)))
			.thenReturn(Mono.just(turn));
		when(retrievalPort.retrieve(eq(testText), eq(3)))
			.thenReturn(Mono.just(emptyContext));
		when(promptTemplate.buildPrompt(any(RetrievalContext.class)))
			.thenReturn("Prompt");
		when(llmPort.streamCompletion(any(CompletionRequest.class)))
			.thenReturn(Flux.just("첫", "번째", ".", " 두", "번째", "!", " 세", "번째", "?"));
		when(ttsPort.streamSynthesize("첫번째."))
			.thenReturn(Flux.just(audio1));
		when(ttsPort.streamSynthesize("두번째!"))
			.thenReturn(Flux.just(audio2));
		when(ttsPort.streamSynthesize("세번째?"))
			.thenReturn(Flux.just(audio3));

		StepVerifier.create(service.executeAudioStreaming(testText))
			.expectNext(audio1)
			.expectNext(audio2)
			.expectNext(audio3)
			.verifyComplete();
	}
}
