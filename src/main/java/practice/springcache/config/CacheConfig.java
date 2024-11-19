package practice.springcache.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory basicCacheRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public CacheManager cacheManager() {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)) //캐시에 적용할 TTL(Time To Live) 설정
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) //캐시 키의 직렬화/역직렬화에 사용되는 값을 정의
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class))); //캐시 값의 직렬화/역직렬화에 사용되는 값을 정의

        Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
        configurationMap.put("itemCache", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(basicCacheRedisConnectionFactory())
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(configurationMap) //미리 추가할 캐시 정보
            .build();
    }
}
