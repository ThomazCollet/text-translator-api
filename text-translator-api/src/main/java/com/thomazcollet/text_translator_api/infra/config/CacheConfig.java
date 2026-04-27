package com.thomazcollet.text_translator_api.infra.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Configuração estratégica de Cache utilizando Redis.
 * Esta classe define como as traduções e áudios serão armazenados,
 * garantindo performance e redução de custos com APIs externas.
 */
@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        
        // Define a serialização dos valores como JSON para facilitar o debug 
        // e garantir a compatibilidade entre diferentes versões do sistema.
        var serializationContext = RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer());

        // Configuração customizada:
        // 1. TTL de 24h: Evita que o cache cresça infinitamente.
        // 2. JSON Serializer: Armazena os objetos de forma legível no Redis.
        // 3. Null Safety: Impede o armazenamento de respostas nulas no cache.
        var config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(24))
                .serializeValuesWith(serializationContext)
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}