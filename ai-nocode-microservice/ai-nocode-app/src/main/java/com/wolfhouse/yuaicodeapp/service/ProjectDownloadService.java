package com.wolfhouse.yuaicodeapp.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author linexsong
 */
public interface ProjectDownloadService {
    /**
     * 下载项目为 ZIP 压缩包文件。
     *
     * @param projectUrl  项目的 URL 地址，用于指定要下载的项目资源。
     * @param projectName 项目的名称，将作为 ZIP 文件的下载名。
     * @param response    HTTP 响应对象，用于将 ZIP 文件流写入客户端。
     */
    void downloadProjectAsZip(String projectUrl, String projectName, HttpServletResponse response);
}
