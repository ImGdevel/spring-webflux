package com.study.webflux.voice.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class VoicePipelineV2ServiceTest {

	private static final List<String> EXPECTED_SENTENCES = List.of("Hello world.", "Second sentence.");
	private static final List<String> TOKENS = List.of("Hello ", "world", ". ", "Second ", "sentence", ".");

	@Test
	void runPipelineBase64EmitsEncodedSentences() {
		RecordingTtsStreamingClient ttsClient = new RecordingTtsStreamingClient();
		VoicePipelineV2Service service = new VoicePipelineV2Service(
			new StubLlmStreamingClient(),
			ttsClient,
			new SentenceAssemblyService()
		);

		VoiceV2Request request = new VoiceV2Request("ignored", Instant.EPOCH);

		StepVerifier.create(service.runPipelineBase64(request))
			.assertNext(base64 -> assertEquals(EXPECTED_SENTENCES.get(0), decode(base64)))
			.assertNext(base64 -> assertEquals(EXPECTED_SENTENCES.get(1), decode(base64)))
			.verifyComplete();

		assertEquals(EXPECTED_SENTENCES, ttsClient.getReceivedSentences());
	}

	private static String decode(String base64) {
		return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
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
