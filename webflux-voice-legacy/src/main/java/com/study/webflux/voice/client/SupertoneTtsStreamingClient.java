package com.study.webflux.voice.client;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.webflux.voice.common.VoiceConstants;
import com.study.webflux.voice.config.RagVoiceProperties;

import reactor.core.publisher.Flux;

@Component
public class SupertoneTtsStreamingClient implements TtsStreamingClient {

	private final WebClient webClient;
	private final ObjectMapper objectMapper;
	private final RagVoiceProperties properties;

	public SupertoneTtsStreamingClient(
		WebClient.Builder webClientBuilder,
		ObjectMapper objectMapper,
		RagVoiceProperties properties
	) {
		this.objectMapper = objectMapper;
		this.properties = properties;
		this.webClient = webClientBuilder
			.baseUrl(properties.getSupertone().getBaseUrl())
			.defaultHeader("x-sup-api-key", properties.getSupertone().getApiKey())
			.build();
	}

	@Override
	public Flux<byte[]> streamAudio(String sentence) {
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
		payload.put("output_format", properties.getSupertone().getOutputFormat());
		payload.put("voice_settings", voiceSettings);
		payload.put("include_phonemes", false);

		return webClient.post()
			.uri("/v1/text-to-speech/{voice_id}/stream", properties.getSupertone().getVoiceId())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(payload)
			.accept(getAcceptMediaType())
			.retrieve()
			.bodyToFlux(DataBuffer.class)
			.map(dataBuffer -> {
				byte[] bytes = new byte[dataBuffer.readableByteCount()];
				dataBuffer.read(bytes);
				org.springframework.core.io.buffer.DataBufferUtils.release(dataBuffer);
				return bytes;
			});
	}

	private MediaType getAcceptMediaType() {
		return switch (properties.getSupertone().getOutputFormat()) {
			case VoiceConstants.Supertone.OutputFormat.MP3 -> MediaType.parseMediaType("audio/mpeg");
			case VoiceConstants.Supertone.OutputFormat.WAV -> MediaType.parseMediaType("audio/wav");
			default -> MediaType.APPLICATION_OCTET_STREAM;
		};
	}
}
