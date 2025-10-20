package com.wolfhouse.yuaicodemother.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class WebScreenShotUtilsTest {

    @Test
    void saveWebPageScreenShot() {
        String testUrl = "https://wolfblog.cn";
        String webPageScreenShot = WebScreenShotUtils.saveWebPageScreenShot(testUrl);
        Assertions.assertNotNull(webPageScreenShot);
    }
}