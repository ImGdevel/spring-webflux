package com.study.webflux.voice.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class VoicePipelineV2ServiceTest {

	private static final List<String> TOKENS = List.of(
		"Hello ",
		"world",
		". ",
		"Second ",
		"sentence",
		"."
	);

	private static final List<String> EXPECTED_SENTENCES = List.of(
		"Hello world.",
		"Second sentence."
	);

	@Test
	void runPipelineBase64UsesConfiguredChunkSize() {
		VoiceV2Properties properties = new VoiceV2Properties();
		properties.setChunkSize(4);

		RecordingTtsStreamingClient ttsClient = new RecordingTtsStreamingClient();
		VoicePipelineV2Service service = new VoicePipelineV2Service(
			new StubLlmStreamingClient(),
			ttsClient,
			new SentenceAssemblyService(),
			new AudioChunkingService(properties)
		);

		VoiceV2Request request = new VoiceV2Request("ignored", Instant.EPOCH);
		List<String> expectedChunks = encodeWithChunkSize(EXPECTED_SENTENCES, 4);

		StepVerifier.create(service.runPipelineBase64(request))
			.expectNextSequence(expectedChunks)
			.verifyComplete();

		assertEquals(EXPECTED_SENTENCES, ttsClient.getReceivedSentences());
	}

	@Test
	void runPipelineBase64CanDisableChunkingPerCall() {
		VoiceV2Properties properties = new VoiceV2Properties();
		properties.setChunkSize(8); // default won't matter because we override per call

		VoicePipelineV2Service service = new VoicePipelineV2Service(
			new StubLlmStreamingClient(),
			new RecordingTtsStreamingClient(),
			new SentenceAssemblyService(),
			new AudioChunkingService(properties)
		);

		VoiceV2Request request = new VoiceV2Request("ignored", Instant.EPOCH);
		List<String> expectedChunks = encodeWithChunkSize(EXPECTED_SENTENCES, 0);

		StepVerifier.create(service.runPipelineBase64(request, 0))
			.expectNextSequence(expectedChunks)
			.verifyComplete();
	}

	private static List<String> encodeWithChunkSize(List<String> sentences, int chunkSize) {
		if (chunkSize <= 0) {
			return sentences.stream()
				.map(sentence -> Base64.getEncoder().encodeToString(sentence.getBytes(StandardCharsets.UTF_8)))
				.collect(Collectors.toList());
		}

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		List<String> encoded = new ArrayList<>();

		for (String sentence : sentences) {
			byte[] bytes = sentence.getBytes(StandardCharsets.UTF_8);
			buffer.write(bytes, 0, bytes.length);

			while (buffer.size() >= chunkSize) {
				byte[] data = buffer.toByteArray();
				byte[] emit = Arrays.copyOf(data, chunkSize);
				encoded.add(Base64.getEncoder().encodeToString(emit));

				buffer.reset();
				if (data.length > chunkSize) {
					buffer.write(data, chunkSize, data.length - chunkSize);
				}
			}
		}

		if (buffer.size() > 0) {
			byte[] remaining = buffer.toByteArray();
			encoded.add(Base64.getEncoder().encodeToString(remaining));
		}

		return encoded;
	}

	private static final class StubLlmStreamingClient implements LlmStreamingClient {
		@Override
		public Flux<String> streamCompletion(String prompt) {
			return Flux.fromIterable(TOKENS);
		}
	}

	private static final class RecordingTtsStreamingClient implements TtsStreamingClient {

		private final List<String> receivedSentences = new ArrayList<>();

		@Override
		public Flux<byte[]> streamAudio(String sentence) {
			receivedSentences.add(sentence);
			return Flux.just(sentence.getBytes(StandardCharsets.UTF_8));
		}

		public List<String> getReceivedSentences() {
			return receivedSentences;
		}
	}
}
