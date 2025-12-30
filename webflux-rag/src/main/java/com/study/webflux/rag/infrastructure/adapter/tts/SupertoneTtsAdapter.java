package com.study.webflux.rag.infrastructure.adapter.tts;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.study.webflux.rag.domain.model.voice.Voice;
import com.study.webflux.rag.domain.port.out.TtsPort;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SupertoneTtsAdapter implements TtsPort {

	private final WebClient webClient;
	private final Voice voice;

	public SupertoneTtsAdapter(WebClient.Builder webClientBuilder, SupertoneConfig config, Voice voice) {
		this.voice = voice;
		this.webClient = webClientBuilder
			.baseUrl(config.baseUrl())
			.defaultHeader("x-sup-api-key", config.apiKey())
			.build();
	}

	@Override
	public Flux<byte[]> streamSynthesize(String text) {
		var settings = voice.getSettings();
		var voiceSettings = Map.of(
			"pitch_shift", settings.pitchShift(),
			"pitch_variance", settings.pitchVariance(),
			"speed", settings.speed()
		);

		var payload = new HashMap<String, Object>();
		payload.put("text", text);
		payload.put("language", voice.getLanguage());
		payload.put("style", voice.getStyle().getValue());
		payload.put("output_format", voice.getOutputFormat().name().toLowerCase());
		payload.put("voice_settings", voiceSettings);
		payload.put("include_phonemes", false);

		return webClient.post()
			.uri("/v1/text-to-speech/{voice_id}/stream", voice.getId())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(payload)
			.accept(MediaType.parseMediaType(voice.getOutputFormat().getMediaType()))
			.retrieve()
			.bodyToFlux(DataBuffer.class)
			.map(dataBuffer -> {
				byte[] bytes = new byte[dataBuffer.readableByteCount()];
				dataBuffer.read(bytes);
				org.springframework.core.io.buffer.DataBufferUtils.release(dataBuffer);
				return bytes;
			});
	}

	@Override
	public Mono<byte[]> synthesize(String text) {
		return streamSynthesize(text)
			.collectList()
			.map(byteArrays -> {
				int totalSize = byteArrays.stream().mapToInt(arr -> arr.length).sum();
				byte[] result = new byte[totalSize];
				int offset = 0;
				for (byte[] arr : byteArrays) {
					System.arraycopy(arr, 0, result, offset, arr.length);
					offset += arr.length;
				}
				return result;
			});
	}
}
