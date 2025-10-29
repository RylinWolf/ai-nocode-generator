package com.wolfhouse.yuaicodemother.ai.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author linexsong
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {
    private Long ttl;
    private Integer port;
    private String host;
    private String password;
    private String username;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                                   .password(password)
                                   .port(port)
                                   .host(host)
                                   .ttl(ttl)
                                   .user(username)
                                   .build();
    }
}
