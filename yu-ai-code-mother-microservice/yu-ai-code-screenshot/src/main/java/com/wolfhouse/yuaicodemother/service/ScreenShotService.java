package com.wolfhouse.yuaicodemother.service;

/**
 * 截图服务
 *
 * @author Rylin Wolf
 */
public interface ScreenShotService {
    /**
     * 通用截图服务，得到访问地址
     *
     * @param url 网址
     * @return 文件位置
     */
    String generateAndUploadScreenshot(String url);
}
