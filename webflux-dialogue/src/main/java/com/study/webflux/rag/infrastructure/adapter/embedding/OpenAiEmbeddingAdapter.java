package com.study.webflux.rag.infrastructure.adapter.embedding;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.study.webflux.rag.domain.model.memory.MemoryEmbedding;
import com.study.webflux.rag.domain.port.out.EmbeddingPort;
import com.study.webflux.rag.infrastructure.adapter.embedding.dto.EmbeddingRequest;
import com.study.webflux.rag.infrastructure.adapter.embedding.dto.EmbeddingResponse;

import reactor.core.publisher.Mono;

@Component
public class OpenAiEmbeddingAdapter implements EmbeddingPort {

	private final WebClient webClient;
	private final String model;

	public OpenAiEmbeddingAdapter(
		WebClient.Builder webClientBuilder,
		OpenAiEmbeddingConfig config
	) {
		this.model = config.model();
		this.webClient = webClientBuilder
			.baseUrl(config.baseUrl())
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.apiKey())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	@Override
	public Mono<MemoryEmbedding> embed(String text) {
		EmbeddingRequest request = new EmbeddingRequest(text, model);

		return webClient.post()
			.uri("/embeddings")
			.bodyValue(request)
			.retrieve()
			.bodyToMono(EmbeddingResponse.class)
			.map(response -> {
				if (response.data().isEmpty()) {
					throw new RuntimeException("No embedding data returned");
				}
				return MemoryEmbedding.of(text, response.data().get(0).embedding());
			});
	}
}
