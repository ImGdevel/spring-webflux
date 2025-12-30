package com.study.webflux.rag.infrastructure.adapter.vectordb;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.study.webflux.rag.domain.model.memory.Memory;
import com.study.webflux.rag.domain.model.memory.MemoryType;
import com.study.webflux.rag.domain.port.out.VectorMemoryPort;
import com.study.webflux.rag.infrastructure.adapter.vectordb.dto.QdrantFilter;
import com.study.webflux.rag.infrastructure.adapter.vectordb.dto.QdrantPoint;
import com.study.webflux.rag.infrastructure.adapter.vectordb.dto.QdrantScoredPoint;
import com.study.webflux.rag.infrastructure.adapter.vectordb.dto.QdrantSearchRequest;
import com.study.webflux.rag.infrastructure.adapter.vectordb.dto.QdrantSearchResponse;
import com.study.webflux.rag.infrastructure.adapter.vectordb.dto.QdrantUpdatePayloadRequest;
import com.study.webflux.rag.infrastructure.adapter.vectordb.dto.QdrantUpsertRequest;
import com.study.webflux.rag.infrastructure.adapter.vectordb.dto.QdrantUpsertResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class QdrantVectorDbAdapter implements VectorMemoryPort {

	private final WebClient webClient;
	private final String collectionName;

	public QdrantVectorDbAdapter(WebClient.Builder webClientBuilder, QdrantConfig config) {
		WebClient.Builder builder = webClientBuilder
			.baseUrl(config.url())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		if (config.apiKey() != null && !config.apiKey().isBlank()) {
			builder.defaultHeader("api-key", config.apiKey());
		}

		this.webClient = builder.build();
		this.collectionName = config.collectionName();
	}

	@Override
	public Mono<Memory> upsert(Memory memory, List<Float> embedding) {
		String id = memory.id() != null ? memory.id() : UUID.randomUUID().toString();

		Map<String, Object> payload = new HashMap<>();
		payload.put("content", memory.content());
		payload.put("type", memory.type().name());

		if (memory.importance() != null) {
			payload.put("importance", memory.importance());
		}
		if (memory.createdAt() != null) {
			payload.put("createdAt", memory.createdAt().getEpochSecond());
		}
		if (memory.lastAccessedAt() != null) {
			payload.put("lastAccessedAt", memory.lastAccessedAt().getEpochSecond());
		}
		if (memory.accessCount() != null) {
			payload.put("accessCount", memory.accessCount());
		}

		QdrantPoint point = new QdrantPoint(id, embedding, payload);
		QdrantUpsertRequest request = new QdrantUpsertRequest(List.of(point));

		return webClient.put()
			.uri("/collections/{collection}/points", collectionName)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(QdrantUpsertResponse.class)
			.map(response -> memory.withId(id));
	}

	@Override
	public Flux<Memory> search(List<Float> queryEmbedding, List<MemoryType> types, float importanceThreshold,
		int topK) {
		List<String> typeStrings = types.stream()
			.map(MemoryType::name)
			.collect(Collectors.toList());

		QdrantFilter filter = new QdrantFilter(List.of(
			QdrantFilter.FilterCondition.matchAny("type", typeStrings),
			QdrantFilter.FilterCondition.rangeGte("importance", importanceThreshold)
		));

		QdrantSearchRequest request = new QdrantSearchRequest(
			queryEmbedding,
			topK,
			true,
			filter
		);

		return webClient.post()
			.uri("/collections/{collection}/points/search", collectionName)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(QdrantSearchResponse.class)
			.flatMapMany(response -> Flux.fromIterable(response.result()))
			.map(this::toMemory);
	}

	@Override
	public Mono<Void> updateImportance(String memoryId, float newImportance, Instant lastAccessedAt, int accessCount) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("importance", newImportance);
		payload.put("lastAccessedAt", lastAccessedAt.getEpochSecond());
		payload.put("accessCount", accessCount);

		QdrantUpdatePayloadRequest request = new QdrantUpdatePayloadRequest(
			List.of(memoryId),
			payload
		);

		return webClient.post()
			.uri("/collections/{collection}/points/payload", collectionName)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(Void.class)
			.onErrorResume(e -> Mono.empty());
	}

	private Memory toMemory(QdrantScoredPoint point) {
		Map<String, Object> payload = point.payload();

		String type = (String)payload.get("type");
		String content = (String)payload.get("content");

		Float importance = null;
		if (payload.containsKey("importance")) {
			importance = ((Number)payload.get("importance")).floatValue();
		}

		Instant createdAt = null;
		if (payload.containsKey("createdAt")) {
			createdAt = Instant.ofEpochSecond(((Number)payload.get("createdAt")).longValue());
		}

		Instant lastAccessedAt = null;
		if (payload.containsKey("lastAccessedAt")) {
			lastAccessedAt = Instant.ofEpochSecond(((Number)payload.get("lastAccessedAt")).longValue());
		}

		Integer accessCount = null;
		if (payload.containsKey("accessCount")) {
			accessCount = ((Number)payload.get("accessCount")).intValue();
		}

		return new Memory(
			point.id(),
			MemoryType.valueOf(type),
			content,
			importance,
			createdAt,
			lastAccessedAt,
			accessCount
		);
	}
}
