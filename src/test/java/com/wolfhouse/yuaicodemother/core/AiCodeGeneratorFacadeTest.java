package com.wolfhouse.yuaicodemother.core;

import com.wolfhouse.yuaicodemother.model.enums.CodeGenTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@SpringBootTest
class AiCodeGeneratorFacadeTest {
    @Autowired
    AiCodeGeneratorFacade facade;

    @Test
    void generateAndSaveCode() {
        File file = facade.generateAndSaveCode("生成一个博客页面，不超过 50 行", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = facade.generateAndSaveCodeStream("生成一个博客页面，不超过 50 行",
                                                                   CodeGenTypeEnum.MULTI_FILE);
        List<String> result = codeStream.collectList()
                                        .block();
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }
}