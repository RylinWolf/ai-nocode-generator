package com.wolfhouse.yuaicodeapp.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.wolfhouse.yuaicodemother.ai.model.HtmlCodeResult;
import com.wolfhouse.yuaicodemother.ai.model.MultiFileCodeResult;
import com.wolfhouse.yuaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * 文件保存器
 *
 * @author linexsong
 */
@Deprecated
public class CodeFileSaver {
    /** 文件保存的根目录 */
    public static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/temp/code_output/";

    /**
     * 保存 HTML 网页代码
     */
    public static File saveHtml(HtmlCodeResult result) {
        String dirPath = buildFilePath(CodeGenTypeEnum.HTML.getValue());
        writeToFile(dirPath, "index.html", result.getHtmlCode());
        return new File(dirPath);
    }

    /**
     * 保存多文件代码
     */
    public static File saveMultiFile(MultiFileCodeResult result) {
        String dirPath = buildFilePath(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(dirPath, "index.html", result.getHtmlCode());
        writeToFile(dirPath, "script.js", result.getJsCode());
        writeToFile(dirPath, "style.css", result.getCssCode());
        return new File(dirPath);
    }


    /**
     * 构建文件的唯一路径（tmp/code_output/bizType_雪花 ID）
     *
     * @param bizType 代码生成类型
     * @return 文件路径
     */
    private static String buildFilePath(String bizType) {
        String dirPath = Path.of(FILE_SAVE_ROOT_DIR,
                                 bizType + "_" + IdUtil.getSnowflakeNextIdStr())
                             .toString();
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /** 保存单个文件 */
    public static void writeToFile(String dirPath, String fileName, String content) {
        String filePath = Path.of(dirPath, fileName)
                              .toString();
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
