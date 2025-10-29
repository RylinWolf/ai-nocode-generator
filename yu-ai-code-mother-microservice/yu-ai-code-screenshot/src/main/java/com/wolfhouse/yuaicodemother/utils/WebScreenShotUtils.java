package com.wolfhouse.yuaicodemother.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Path;
import java.time.Duration;

/**
 * 截图工具类
 *
 * @author linexsong
 */
@Slf4j
public class WebScreenShotUtils {

    private static final WebDriver WEB_DRIVER;
    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 1000;

    static {
        // 初始化驱动，避免重复初始化
        WEB_DRIVER = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver()
                            .setup();
            // 配置 Chrome 选项
            WebDriver driver = getWebDriver(width, height);
            // 设置页面加载超时
            driver.manage()
                  .timeouts()
                  .pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage()
                  .timeouts()
                  .implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    private static WebDriver getWebDriver(int width, int height) {
        ChromeOptions options = new ChromeOptions();
        // 无头模式
        options.addArguments("--headless");
        // 禁用GPU（在某些环境下避免问题）
        options.addArguments("--disable-gpu");
        // 禁用沙盒模式（Docker环境需要）
        options.addArguments("--no-sandbox");
        // 禁用开发者shm使用
        options.addArguments("--disable-dev-shm-usage");
        // 设置窗口大小
        options.addArguments(String.format("--window-size=%d,%d", width, height));
        // 禁用扩展
        options.addArguments("--disable-extensions");
        // 设置用户代理
        options.addArguments(
            "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/91.0.4472.124 Safari/537.36");
        // 创建驱动
        return new ChromeDriver(options);
    }

    /**
     * 保存网页截图到本地文件并返回文件路径。
     *
     * @param webUrl 需要截取网页的 URL 地址
     * @return 保存截图的本地文件路径
     */
    public static String saveWebPageScreenShot(String webUrl) {
        // 非空校验
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页截图失败，url 为空");
            return null;
        }
        try {
            // 创建临时目录
            String tempDir = Path.of(System.getProperty("user.dir"),
                                     "/temp/screenshots/",
                                     UUID.randomUUID()
                                         .toString()
                                         .substring(0, 8))
                                 .toString();

            FileUtil.mkdir(tempDir);
            // 图片后缀
            final String IMAGE_SUFFIX = ".png";
            String imageSavePath = Path.of(tempDir, RandomUtil.randomNumbers(5) + IMAGE_SUFFIX)
                                       .toString();
            // 访问网页
            WEB_DRIVER.get(webUrl);
            // 等待网页加载
            waitForPageLoad(WEB_DRIVER);
            // 截图
            byte[] screenshotBytes = ((TakesScreenshot) WEB_DRIVER).getScreenshotAs(OutputType.BYTES);
            saveImage(screenshotBytes, imageSavePath);
            log.info("原始截图保存成功: {}", imageSavePath);
            // 压缩图片
            final String COMPRESSED_IMAGE_SUFFIX = "_compressed.jpg";
            String compressImgPath = Path.of(tempDir, RandomUtil.randomNumbers(5) + COMPRESSED_IMAGE_SUFFIX)
                                         .toString();
            compressImage(imageSavePath, compressImgPath);
            // 删除原始图片
            FileUtil.del(imageSavePath);
            return compressImgPath;
        } catch (Exception e) {
            log.error("网页截图失败", e);
            return null;
        }
    }

    private static void compressImage(String originImagePath, String compressedImagePath) {
        final float COMPRESS_QUALITY = 0.5f;
        try {
            ImgUtil.compress(FileUtil.file(originImagePath),
                             FileUtil.file(compressedImagePath),
                             COMPRESS_QUALITY);
        } catch (Exception e) {
            log.error("压缩图片失败: {}", originImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }

    }

    private static void waitForPageLoad(WebDriver webDriver) {
        try {
            // 创建等待页面加载对象
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            // 等待 document.readyState 为 true
            wait.until(webDriver1 -> (
                "complete".equals(((JavascriptExecutor) webDriver1)
                                      .executeScript("return document.readyState"))));
            // 等待一段时间，确保动态内容加载完成
            Thread.sleep(1000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常", e);
        }
    }

    /**
     * 保存图片到文件
     *
     * @param imageBytes 图片数据
     * @param imagePath  保存路径
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败: {}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    @PreDestroy
    public void destroy() {
        WEB_DRIVER.quit();
    }

}
