package com.wolfhouse.yuaicodemother.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.wolfhouse.yuaicodemother.config.CosClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Cos 对象存储管理器
 *
 * @author linexsong
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CosManager {
    private final CosClientConfig config;
    private final COSClient cosClient;

    /**
     * 将文件对象上传到指定的 COS 存储桶。
     *
     * @param key  文件在存储桶中的唯一标识符
     * @param file 要上传的文件对象
     * @return 上传操作的结果，包含文件相关的元信息
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(config.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    public String uploadFile(String key, File file) {
        PutObjectResult res = putObject(key, file);
        if (res == null) {
            log.error("文件上传失败，返回结果为空");
            return null;
        }
        String url = String.format("%s%s", config.getHost(), key);
        log.info("文件上传至 COS 成功: {} -> {}", file.getName(), url);
        return url;
    }
}
