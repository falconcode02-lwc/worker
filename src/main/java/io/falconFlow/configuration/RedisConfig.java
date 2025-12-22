package io.falconFlow.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@Profile("cache-redis")
public class RedisConfig implements CachingConfigurer {

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    // Configure ObjectMapper for Redis serialization
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules(); // Registers JavaTimeModule and other modules
    objectMapper.activateDefaultTyping(
        BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)
            .build(),
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY);

    GenericJackson2JsonRedisSerializer serializer =
        new GenericJackson2JsonRedisSerializer(objectMapper);

    org.springframework.data.redis.cache.RedisCacheConfiguration config =
        org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues();

    return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
  }
}
