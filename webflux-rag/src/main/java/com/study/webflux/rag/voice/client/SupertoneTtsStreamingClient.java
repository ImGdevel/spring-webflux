package com.study.webflux.rag.voice.client;

import java.util.Base64;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.webflux.rag.voice.config.RagVoiceProperties;

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
		var payload = Map.of(
			"text", sentence,
			"language", properties.getSupertone().getLanguage(),
			"output_format", properties.getSupertone().getOutputFormat(),
			"include_phonemes", false
		);

		return webClient.post()
			.uri("/text-to-speech/{voice_id}/stream", properties.getSupertone().getVoiceId())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(payload)
			.retrieve()
			.bodyToFlux(DataBuffer.class)
			.map(dataBuffer -> {
				byte[] bytes = new byte[dataBuffer.readableByteCount()];
				dataBuffer.read(bytes);
				return bytes;
			});
	}
}
