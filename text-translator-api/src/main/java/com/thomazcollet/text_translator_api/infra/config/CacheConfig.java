package com.thomazcollet.text_translator_api.infra.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Configuração estratégica de Cache utilizando Redis.
 * Garante performance, persistência temporária e redução de custos operacionais.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
public class CacheConfig {

    private static final int TTL_HOURS = 24;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        
        // Serialização via JSON para transparência e compatibilidade entre versões.
        var serializationContext = RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer());

        // Configuração: TTL de 24h, Serialização JSON e bloqueio de valores nulos.
        var config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(TTL_HOURS))
                .serializeValuesWith(serializationContext)
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}