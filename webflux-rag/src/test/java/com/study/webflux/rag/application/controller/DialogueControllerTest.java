package com.study.webflux.rag.application.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.study.webflux.rag.application.dto.RagDialogueRequest;
import com.study.webflux.rag.domain.port.in.DialoguePipelineUseCase;

import reactor.core.publisher.Flux;

@WebFluxTest(DialogueController.class)
class DialogueControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private DialoguePipelineUseCase dialoguePipelineUseCase;

	@Test
	void ragDialogueStream_shouldReturnSSEStream() {
		String testText = "Hello world";
		RagDialogueRequest request = new RagDialogueRequest(testText, Instant.now());

		String base64Audio = Base64.getEncoder().encodeToString("audio-data".getBytes());

		when(dialoguePipelineUseCase.executeStreaming(eq(testText)))
			.thenReturn(Flux.just(base64Audio));

		webTestClient.post()
			.uri("/rag/dialogue/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk();

		verify(dialoguePipelineUseCase).executeStreaming(testText);
	}

	@Test
	void ragDialogueAudioWav_shouldReturnWavAudio() {
		String testText = "Test audio";
		RagDialogueRequest request = new RagDialogueRequest(testText, Instant.now());

		byte[] audioBytes = "wav-audio-data".getBytes();

		when(dialoguePipelineUseCase.executeAudioStreaming(eq(testText)))
			.thenReturn(Flux.just(audioBytes));

		webTestClient.post()
			.uri("/rag/dialogue/audio/wav")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType("audio/wav");

		verify(dialoguePipelineUseCase).executeAudioStreaming(testText);
	}

	@Test
	void ragDialogueAudioMp3_shouldReturnMp3Audio() {
		String testText = "Test MP3";
		RagDialogueRequest request = new RagDialogueRequest(testText, Instant.now());

		byte[] audioBytes = "mp3-audio-data".getBytes();

		when(dialoguePipelineUseCase.executeAudioStreaming(eq(testText)))
			.thenReturn(Flux.just(audioBytes));

		webTestClient.post()
			.uri("/rag/dialogue/audio/mp3")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType("audio/mpeg");

		verify(dialoguePipelineUseCase).executeAudioStreaming(testText);
	}

	@Test
	void ragDialogueAudio_shouldDelegateToWav() {
		String testText = "Default audio";
		RagDialogueRequest request = new RagDialogueRequest(testText, Instant.now());

		byte[] audioBytes = "default-audio".getBytes();

		when(dialoguePipelineUseCase.executeAudioStreaming(eq(testText)))
			.thenReturn(Flux.just(audioBytes));

		webTestClient.post()
			.uri("/rag/dialogue/audio")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType("audio/wav");

		verify(dialoguePipelineUseCase).executeAudioStreaming(testText);
	}

	@Test
	void ragDialogueStream_withBlankText_shouldReturnBadRequest() {
		RagDialogueRequest request = new RagDialogueRequest("", Instant.now());

		webTestClient.post()
			.uri("/rag/dialogue/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Test
	void ragDialogueStream_withNullTimestamp_shouldReturnBadRequest() {
		RagDialogueRequest request = new RagDialogueRequest("test", null);

		webTestClient.post()
			.uri("/rag/dialogue/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest();
	}
}
