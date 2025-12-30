package com.study.webflux.rag.infrastructure.adapter.vectordb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.study.webflux.rag.infrastructure.adapter.vectordb.dto.QdrantCreateCollectionRequest;

import jakarta.annotation.PostConstruct;

@Component
public class QdrantCollectionInitializer {

	private static final Logger log = LoggerFactory.getLogger(QdrantCollectionInitializer.class);

	private final WebClient webClient;
	private final String collectionName;
	private final int vectorDimension;

	public QdrantCollectionInitializer(WebClient.Builder builder, QdrantConfig config) {
		WebClient.Builder clientBuilder = builder
			.baseUrl(config.url())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		if (config.apiKey() != null && !config.apiKey().isBlank()) {
			clientBuilder.defaultHeader("api-key", config.apiKey());
		}

		this.webClient = clientBuilder.build();
		this.collectionName = config.collectionName();
		this.vectorDimension = config.vectorDimension();
	}

	@PostConstruct
	public void initializeCollection() {
		QdrantCreateCollectionRequest request = new QdrantCreateCollectionRequest(
			new QdrantCreateCollectionRequest.VectorParams(vectorDimension, "Cosine")
		);

		webClient.put()
			.uri("/collections/{collection}", collectionName)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(String.class)
			.subscribe(
				response -> log.info("Qdrant collection '{}' initialized", collectionName),
				error -> log.warn("Failed to create collection '{}': {}", collectionName, error.getMessage())
			);
	}
}
