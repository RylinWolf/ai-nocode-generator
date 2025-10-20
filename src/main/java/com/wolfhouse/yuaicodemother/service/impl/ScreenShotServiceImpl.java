package com.wolfhouse.yuaicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.exception.ThrowUtils;
import com.wolfhouse.yuaicodemother.manager.CosManager;
import com.wolfhouse.yuaicodemother.service.ScreenShotService;
import com.wolfhouse.yuaicodemother.utils.WebScreenShotUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author Rylin Wolf
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenShotServiceImpl implements ScreenShotService {
    private final CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String url) {
        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR, "网址不能为空");
        // 本地截图
        String localScreenshotPath = WebScreenShotUtils.saveWebPageScreenShot(url);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "本地截图生成失败");
        // 上传到对象存储
        try {
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "上传图片至 COS 失败");
            log.info("截图上传成功");
            return cosUrl;
        } finally {
            cleanupLocalFile(localScreenshotPath);
        }
    }


    /**
     * 上传截图到对象存储
     *
     * @param localScreenshotPath 本地截图路径
     * @return 对象存储访问URL，失败返回null
     */
    private String uploadScreenshotToCos(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File localScreenshotFile = new File(localScreenshotPath);
        if (!localScreenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
        }
        // 生成 COS 对象键
        String filename = UUID.randomUUID()
                              .toString()
                              .substring(0, 8) + "_compressed.jpg";
        String screenshotKey = generateScreenshotKey(filename);
        return cosManager.uploadFile(screenshotKey, localScreenshotFile);
    }

    /**
     * 生成截图的对象存储键
     * 格式：/screenshots/2025/07/31/filename.jpg
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now()
                                   .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地文件
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理: {}", localFilePath);
        }
    }


}
