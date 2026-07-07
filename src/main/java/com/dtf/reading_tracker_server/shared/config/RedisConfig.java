package com.dtf.reading_tracker_server.shared.config;

import com.dtf.reading_tracker_server.shared.cache.CacheNames;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = cacheConfig(Duration.ofMinutes(10));

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                CacheNames.OPEN_LIBRARY_SEARCH, cacheConfig(Duration.ofHours(6)),
                CacheNames.BOOKS_BY_ID, cacheConfig(Duration.ofHours(1)),
                CacheNames.USER_BOOKS_BY_USER, cacheConfig(Duration.ofMinutes(5)),
                CacheNames.USER_BOOK_BY_USER_AND_BOOK, cacheConfig(Duration.ofMinutes(5))
        );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private RedisCacheConfiguration cacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                GenericJacksonJsonRedisSerializer.builder()
                                        .customize(builder -> builder.findAndAddModules())
                                        .build()
                        )
                )
                .entryTtl(ttl)
                .disableCachingNullValues();
    }
}
