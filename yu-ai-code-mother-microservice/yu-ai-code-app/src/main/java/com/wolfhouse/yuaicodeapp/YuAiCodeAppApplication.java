package com.wolfhouse.yuaicodeapp;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Rylin Wolf
 */
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.wolfhouse.yuaicodeapp.mapper")
@ConfigurationPropertiesScan(basePackages = "com.wolfhouse.yuaicodeapp.config")
@EnableCaching
@ComponentScan("com.wolfhouse")
public class YuAiCodeAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuAiCodeAppApplication.class, args);
    }
}
