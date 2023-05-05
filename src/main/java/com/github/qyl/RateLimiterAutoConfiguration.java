package com.github.qyl;

import com.github.qyl.core.BizKeyProvider;
import com.github.qyl.core.RateLimitAspectHandler;
import com.github.qyl.core.RateLimiterService;
import com.github.qyl.web.RateLimitExceptionHandler;
import io.micrometer.core.instrument.util.StringUtils;
import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@ConditionalOnProperty(prefix = RateLimiterProperties.PREFIX, name = "enabled", havingValue = "true")
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(RateLimiterProperties.class)
@Import({RateLimitAspectHandler.class, RateLimitExceptionHandler.class})
public class RateLimiterAutoConfiguration {

    @Value("${spring.redis.host:localhost}")
    private String host;
    @Value("${spring.redis.port:6379}")
    private String port;
    @Value("${spring.redis.password:}")
    private String password;

    private final RateLimiterProperties limiterProperties;

    public RateLimiterAutoConfiguration(RateLimiterProperties limiterProperties) {
        this.limiterProperties = limiterProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
        redisTemplate.setConnectionFactory(factory);
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(redisSerializer);
        redisTemplate.setHashKeySerializer(redisSerializer);
        return redisTemplate;
    }

    @ConditionalOnMissingBean
    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() {
        Config config = new Config();
        if (limiterProperties.getRedisClusterServer() != null) {
            config.useClusterServers()
                    .addNodeAddress(limiterProperties.getRedisClusterServer().getNodeAddresses());
            if (StringUtils.isNotBlank(password)) {
                config.useClusterServers().setPassword(password);
            }
        } else {
            config.useSingleServer().setAddress("redis://" + host + ":" + port);
            if(StringUtils.isNotBlank(password)){
                config.useSingleServer().setPassword(password);
            }
        }

        config.setCodec(new JsonJacksonCodec());
        config.setEventLoopGroup(new NioEventLoopGroup());
        return Redisson.create(config);
    }

    @Bean
    public RateLimiterService rateLimiterInfoProvider() {
        return new RateLimiterService();
    }

    @Bean
    public BizKeyProvider bizKeyProvider() {
        return new BizKeyProvider();
    }

}
