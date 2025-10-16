package com.wolfhouse.yuaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.wolfhouse.yuaicodemother.common.constant.AppConstant;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.exception.ThrowUtils;
import com.wolfhouse.yuaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * 抽象代码文件保存器 - 模板方法模式
 *
 * @author linexsong
 */
public abstract class AbstractCodeFileSaverTemplate<T> {
    /** 文件保存的根目录 */
    public static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 构建文件的唯一路径（tmp/code_output/bizType_雪花 ID）
     *
     * @return 文件路径
     */
    protected String buildUniqueDir(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        String bizType = getCodeType().getValue();
        String dirPath = Path.of(FILE_SAVE_ROOT_DIR,
                                 bizType + "_" + appId)
                             .toString();
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 保存代码结果到唯一生成的目录，并返回目录的文件对象。
     * 该方法为模板方法，定义了代码保存的通用流程，包括：
     * 1. 验证输入结果对象的合法性。
     * 2. 构建保存文件的唯一路径目录。
     * 3. 调用子类的抽象方法完成实际的文件保存逻辑。
     *
     * @param result 需要保存的代码结果对象，具体类型和内容由子类根据业务场景定义。
     * @param appId  应用 ID
     * @return 一个表示保存目录的文件对象。
     * @throws BusinessException 若输入的代码结果对象为空时抛出异常。
     */
    public final File saveCode(T result, Long appId) {
        // 验证输入
        validateInput(result);
        // 构建唯一目录
        String baseDirPath = buildUniqueDir(appId);
        // 保存文件（具体实现交给子类）
        saveFiles(result, baseDirPath);
        // 返回文件目录对象
        return new File(baseDirPath);
    }

    /**
     * 验证输入结果对象是否为空，如果为空则抛出业务异常。
     *
     * @param result 待验证的结果对象
     * @throws BusinessException 如果结果对象为空则抛出此异常，异常信息包含错误代码和描述
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
        }
    }

    /** 保存单个文件 */
    public final void writeToFile(String dirPath, String fileName, String content) {
        if (StrUtil.isBlank(content)) {
            return;
        }
        String filePath = Path.of(dirPath, fileName)
                              .toString();
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 保存文件的抽象方法，由具体子类实现文件的保存逻辑。
     *
     * @param result      保存操作所需的结果对象，不同子类对其内容的解析与处理方式各不相同
     * @param baseDirPath 用于保存文件的基础目录路径，由框架自动生成，确保目录唯一性
     */
    protected abstract void saveFiles(T result, String baseDirPath);

    /**
     * 获取代码生成的类型枚举。
     *
     * @return 代码生成类型的枚举值
     */
    protected abstract CodeGenTypeEnum getCodeType();

}
