package com.wolfhouse.yuaicodeapp.core.saver;

import com.wolfhouse.yuaicodemother.ai.model.HtmlCodeResult;
import com.wolfhouse.yuaicodemother.ai.model.MultiFileCodeResult;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 *
 * @author yupi
 */
public class CodeFileSaverExecutor {

    private static final HtmlCodeFileSaverTemplate HTML_CODE_FILE_SAVER = new HtmlCodeFileSaverTemplate();

    private static final MultiFileCodeFileSaverTemplate MULTI_FILE_CODE_FILE_SAVER =
        new MultiFileCodeFileSaverTemplate();

    /**
     * 执行代码保存
     *
     * @param codeResult  代码结果对象
     * @param codeGenType 代码生成类型
     * @return 保存的目录
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType, Long appId) {
        return switch (codeGenType) {
            case HTML -> HTML_CODE_FILE_SAVER.saveCode((HtmlCodeResult) codeResult, appId);
            case MULTI_FILE -> MULTI_FILE_CODE_FILE_SAVER.saveCode((MultiFileCodeResult) codeResult, appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}
