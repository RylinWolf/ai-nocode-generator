package com.wolfhouse.yuaicodeuser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Rylin Wolf
 */
@SpringBootApplication
@MapperScan("com.wolfhouse.yuaicodeuser.mapper")
@ComponentScan("com.wolfhouse")
public class YuAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuAiCodeUserApplication.class, args);
    }
}
