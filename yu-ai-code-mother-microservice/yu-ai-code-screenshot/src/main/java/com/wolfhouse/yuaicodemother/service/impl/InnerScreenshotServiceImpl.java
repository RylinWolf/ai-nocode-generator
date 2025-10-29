package com.wolfhouse.yuaicodemother.service.impl;

import com.wolfhouse.yuaicodemother.innerservice.InnerScreenshotService;
import com.wolfhouse.yuaicodemother.service.ScreenShotService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

/**
 * @author Rylin Wolf
 */
@DubboService
@RequiredArgsConstructor
@Component
public class InnerScreenshotServiceImpl implements InnerScreenshotService {
    private final ScreenShotService screenShotService;

    @Override
    public String generateAndUploadScreenshot(String url) {
        return screenShotService.generateAndUploadScreenshot(url);
    }
}
