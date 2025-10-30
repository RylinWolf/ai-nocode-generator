package com.wolfhouse.yuaicodemother;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Rylin Wolf
 */
@SpringBootApplication
@EnableDubbo
@ComponentScan("com.wolfhouse")
public class YuAiCodeScreenshotApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuAiCodeScreenshotApplication.class, args);
    }
}
