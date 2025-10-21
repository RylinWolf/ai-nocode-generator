package com.wolfhouse.yuaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.exception.ThrowUtils;
import com.wolfhouse.yuaicodemother.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author Rylin Wolf
 */
@Slf4j
@Service
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    /**
     * 需要过滤的文件和目录名称
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
        "node_modules",
        ".git",
        ".DS_Store",
        "build",
        "dist",
        "target",
        ".env",
        ".env.example",
        ".mvn",
        ".gitignore",
        ".idea",
        ".vscode");

    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
        ".log",
        ".tmp",
        ".cache");

    @Override
    public void downloadProjectAsZip(String projectUrl, String projectName, HttpServletResponse response) {
        // 基础校验
        ThrowUtils.throwIf(StrUtil.isBlank(projectName), ErrorCode.PARAMS_ERROR, "项目名称不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(projectUrl), ErrorCode.PARAMS_ERROR, "项目路径不能为空");
        File projectDir = new File(projectUrl);
        ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.PARAMS_ERROR, "项目路径不存在！");
        ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.PARAMS_ERROR, "项目路径不是目录！");
        log.info("开始下载项目: {} -> {}", projectName, projectUrl);
        // 设置 HTTP 响应头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + projectName + ".zip");
        // 定义文件过滤器
        FileFilter filter = file -> isPathAllowed(Path.of(projectUrl), file.toPath());
        // 压缩
        try {
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, filter, projectDir);
        } catch (Exception e) {
            log.error("打包下载项目失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 判断指定路径是否被允许访问。通过比较路径是否属于项目根目录以及过滤特定的文件/目录名称与文件扩展名，
     * 确保路径符合安全性要求。
     *
     * @param projectRoot 项目根目录路径
     * @param fullPath    需要检查的完整路径
     * @return 如果指定路径被允许访问则返回 true，否则返回 false
     */
    private boolean isPathAllowed(Path projectRoot, Path fullPath) {
        Path relativize = projectRoot.relativize(fullPath);
        // 检查路径中的每一部分是否符合要求
        for (Path path : relativize) {
            String pathName = path.toString();
            if (IGNORED_NAMES.contains(pathName) ||
                IGNORED_EXTENSIONS.stream()
                                  .anyMatch(pathName.toLowerCase()::endsWith)) {
                return false;
            }
        }
        return true;
    }
}
