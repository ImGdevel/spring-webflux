package com.study.webflux.voice.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

	@Bean
	ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
		ReactiveRedisConnectionFactory connectionFactory
	) {
		RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
			.<String, Object>newSerializationContext(new StringRedisSerializer())
			.value(new GenericJackson2JsonRedisSerializer())
			.build();

		return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
	}

	@Bean
	RedisCacheConfiguration cacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(10))
			.disableCachingNullValues();
	}
}
