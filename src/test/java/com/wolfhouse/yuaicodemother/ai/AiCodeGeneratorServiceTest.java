package com.wolfhouse.yuaicodemother.ai;

import com.wolfhouse.yuaicodemother.ai.model.HtmlCodeResult;
import com.wolfhouse.yuaicodemother.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Autowired
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateCode() {
        HtmlCodeResult res = aiCodeGeneratorService.generateCode("做一个狼博客，不超过 20 行");
        Assertions.assertNotNull(res);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult res = aiCodeGeneratorService.generateMultiFileCode("做一个留言板，不超过 20 行");
        Assertions.assertNotNull(res);
    }

    @Test
    void testChatMemory() {
        HtmlCodeResult res = aiCodeGeneratorService.generateCode("生成一个日记网站，不超过 20行");
        Assertions.assertNotNull(res);
        res = aiCodeGeneratorService.generateCode("不要生成网站，告诉我你刚刚做了什么");
        Assertions.assertNotNull(res);
        res = aiCodeGeneratorService.generateCode("生成一个备忘录网站，不超过 20行代码");
        Assertions.assertNotNull(res);
        res = aiCodeGeneratorService.generateCode("不要生成网站，告诉我你刚刚做了什么");
        Assertions.assertNotNull(res);

    }
}