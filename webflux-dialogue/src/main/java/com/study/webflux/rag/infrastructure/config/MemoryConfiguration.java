package com.study.webflux.rag.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.study.webflux.rag.infrastructure.adapter.embedding.OpenAiEmbeddingConfig;
import com.study.webflux.rag.infrastructure.adapter.memory.MemoryExtractionConfig;
import com.study.webflux.rag.infrastructure.adapter.vectordb.QdrantConfig;
import com.study.webflux.rag.infrastructure.config.properties.RagDialogueProperties;

@Configuration
public class MemoryConfiguration {

	@Bean
	public QdrantConfig qdrantConfig(RagDialogueProperties properties) {
		var qdrant = properties.getQdrant();
		return new QdrantConfig(
			qdrant.getUrl(),
			qdrant.getApiKey(),
			qdrant.getVectorDimension(),
			qdrant.getCollectionName()
		);
	}

	@Bean
	public OpenAiEmbeddingConfig openAiEmbeddingConfig(RagDialogueProperties properties) {
		var openai = properties.getOpenai();
		var memory = properties.getMemory();
		return new OpenAiEmbeddingConfig(
			openai.getApiKey(),
			openai.getBaseUrl(),
			memory.getEmbeddingModel()
		);
	}

	@Bean
	public MemoryExtractionConfig memoryExtractionConfig(RagDialogueProperties properties) {
		var memory = properties.getMemory();
		return new MemoryExtractionConfig(
			memory.getExtractionModel(),
			memory.getConversationThreshold(),
			memory.getImportanceBoost(),
			memory.getImportanceThreshold()
		);
	}

	@Bean
	public ReactiveRedisTemplate<String, Long> reactiveRedisLongTemplate(
		ReactiveRedisConnectionFactory connectionFactory
	) {
		RedisSerializationContext<String, Long> context = RedisSerializationContext
			.<String, Long>newSerializationContext(new StringRedisSerializer())
			.value(new GenericToStringSerializer<>(Long.class))
			.build();

		return new ReactiveRedisTemplate<>(connectionFactory, context);
	}
}
