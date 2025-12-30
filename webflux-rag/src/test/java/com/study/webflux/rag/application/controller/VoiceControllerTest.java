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

import com.study.webflux.rag.application.dto.RagVoiceRequest;
import com.study.webflux.rag.domain.port.in.VoicePipelineUseCase;

import reactor.core.publisher.Flux;

@WebFluxTest(VoiceController.class)
class VoiceControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private VoicePipelineUseCase voicePipelineUseCase;

	@Test
	void ragVoiceStream_shouldReturnSSEStream() {
		String testText = "Hello world";
		RagVoiceRequest request = new RagVoiceRequest(testText, Instant.now());

		String base64Audio = Base64.getEncoder().encodeToString("audio-data".getBytes());

		when(voicePipelineUseCase.executeStreaming(eq(testText)))
			.thenReturn(Flux.just(base64Audio));

		webTestClient.post()
			.uri("/rag/voice/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk();

		verify(voicePipelineUseCase).executeStreaming(testText);
	}

	@Test
	void ragVoiceAudioWav_shouldReturnWavAudio() {
		String testText = "Test audio";
		RagVoiceRequest request = new RagVoiceRequest(testText, Instant.now());

		byte[] audioBytes = "wav-audio-data".getBytes();

		when(voicePipelineUseCase.executeAudioStreaming(eq(testText)))
			.thenReturn(Flux.just(audioBytes));

		webTestClient.post()
			.uri("/rag/voice/audio/wav")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType("audio/wav");

		verify(voicePipelineUseCase).executeAudioStreaming(testText);
	}

	@Test
	void ragVoiceAudioMp3_shouldReturnMp3Audio() {
		String testText = "Test MP3";
		RagVoiceRequest request = new RagVoiceRequest(testText, Instant.now());

		byte[] audioBytes = "mp3-audio-data".getBytes();

		when(voicePipelineUseCase.executeAudioStreaming(eq(testText)))
			.thenReturn(Flux.just(audioBytes));

		webTestClient.post()
			.uri("/rag/voice/audio/mp3")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType("audio/mpeg");

		verify(voicePipelineUseCase).executeAudioStreaming(testText);
	}

	@Test
	void ragVoiceAudio_shouldDelegateToWav() {
		String testText = "Default audio";
		RagVoiceRequest request = new RagVoiceRequest(testText, Instant.now());

		byte[] audioBytes = "default-audio".getBytes();

		when(voicePipelineUseCase.executeAudioStreaming(eq(testText)))
			.thenReturn(Flux.just(audioBytes));

		webTestClient.post()
			.uri("/rag/voice/audio")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType("audio/wav");

		verify(voicePipelineUseCase).executeAudioStreaming(testText);
	}

	@Test
	void ragVoiceStream_withBlankText_shouldReturnBadRequest() {
		RagVoiceRequest request = new RagVoiceRequest("", Instant.now());

		webTestClient.post()
			.uri("/rag/voice/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Test
	void ragVoiceStream_withNullTimestamp_shouldReturnBadRequest() {
		RagVoiceRequest request = new RagVoiceRequest("test", null);

		webTestClient.post()
			.uri("/rag/voice/sse")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest();
	}
}
