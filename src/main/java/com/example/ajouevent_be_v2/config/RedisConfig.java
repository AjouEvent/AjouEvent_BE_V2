package com.example.ajouevent_be_v2.config;

import com.example.ajouevent_be_v2.config.properties.RedisProperties;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.Delay;
import io.lettuce.core.resource.DefaultClientResources;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private static final Duration RECONNECT_DELAY_MIN = Duration.ofMillis(100);
    private static final Duration RECONNECT_DELAY_MAX = Duration.ofSeconds(1);

    private final RedisProperties redisProperties;

    /**
     * 재연결 간격 상한을 1초로 제한한다.
     * 기본값(지수 백오프, 상한 30초)에서는 재연결 간격이 최대 30초까지 벌어져, Redis가 복구된 뒤에도 다음 시도까지 실패가 이어진다.
     */
    @Bean(destroyMethod = "shutdown")
    public ClientResources lettuceClientResources() {
        return DefaultClientResources.builder()
                .reconnectDelay(Delay.exponential(RECONNECT_DELAY_MIN, RECONNECT_DELAY_MAX, 2, TimeUnit.MILLISECONDS))
                .build();
    }

    /**
     * 커넥션 팩토리를 직접 생성하므로 spring.data.redis.timeout 이 자동 적용되지 않는다.
     * 명시하지 않으면 Lettuce 기본 명령 타임아웃(60초)이 적용되어, Redis가 응답하지 않을 때 요청 스레드가 묶인다.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(ClientResources lettuceClientResources) {
        RedisStandaloneConfiguration serverConfig =
                new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        ClientOptions clientOptions = ClientOptions.builder()
                .timeoutOptions(TimeoutOptions.enabled())
                .socketOptions(SocketOptions.builder()
                        .connectTimeout(redisProperties.getConnectTimeout())
                        .build())
                .build();
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(redisProperties.getTimeout())
                .clientOptions(clientOptions)
                .clientResources(lettuceClientResources)
                .build();
        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }
}
