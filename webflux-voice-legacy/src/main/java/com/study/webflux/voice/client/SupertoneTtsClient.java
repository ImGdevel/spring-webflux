package com.study.webflux.voice.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.study.webflux.voice.common.VoiceConstants;
import com.study.webflux.voice.config.RagVoiceProperties;

import reactor.core.publisher.Mono;

@Component
public class SupertoneTtsClient implements TtsClient {

	private final WebClient webClient;
	private final RagVoiceProperties properties;

	public SupertoneTtsClient(
		WebClient.Builder webClientBuilder,
		RagVoiceProperties properties
	) {
		this.properties = properties;
		this.webClient = webClientBuilder
			.baseUrl(properties.getSupertone().getBaseUrl())
			.defaultHeader("x-sup-api-key", properties.getSupertone().getApiKey())
			.build();
	}

	@Override
	public Mono<byte[]> synthesize(String sentence) {
		var settings = properties.getSupertone().getVoiceSettings();
		var voiceSettings = Map.of(
			"pitch_shift", settings.getPitchShift(),
			"pitch_variance", settings.getPitchVariance(),
			"speed", settings.getSpeed()
		);

		var payload = new HashMap<String, Object>();
		payload.put("text", sentence);
		payload.put("language", properties.getSupertone().getLanguage());
		payload.put("style", properties.getSupertone().getStyle());
		payload.put("model", VoiceConstants.Supertone.MODEL);
		payload.put("voice_settings", voiceSettings);

		return webClient.post()
			.uri(uriBuilder -> uriBuilder
				.path("/v1/text-to-speech/{voice_id}")
				.queryParam("output_format", properties.getSupertone().getOutputFormat())
				.build(properties.getSupertone().getVoiceId()))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(payload)
			.accept(getAcceptMediaType())
			.retrieve()
			.bodyToFlux(org.springframework.core.io.buffer.DataBuffer.class)
			.reduce(
				new byte[0],
				(accumulated, dataBuffer) -> {
					byte[] bytes = new byte[dataBuffer.readableByteCount()];
					dataBuffer.read(bytes);
					DataBufferUtils.release(dataBuffer);

					byte[] result = new byte[accumulated.length + bytes.length];
					System.arraycopy(accumulated, 0, result, 0, accumulated.length);
					System.arraycopy(bytes, 0, result, accumulated.length, bytes.length);
					return result;
				}
			);
	}

	private MediaType getAcceptMediaType() {
		return switch (properties.getSupertone().getOutputFormat()) {
			case VoiceConstants.Supertone.OutputFormat.MP3 -> MediaType.parseMediaType("audio/mpeg");
			case VoiceConstants.Supertone.OutputFormat.WAV -> MediaType.parseMediaType("audio/wav");
			default -> MediaType.APPLICATION_OCTET_STREAM;
		};
	}
}
