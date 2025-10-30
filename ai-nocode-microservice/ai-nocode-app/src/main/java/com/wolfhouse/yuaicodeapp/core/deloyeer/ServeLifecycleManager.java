package com.wolfhouse.yuaicodeapp.core.deloyeer;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author rylinwolf
 */
@Component
public class ServeLifecycleManager {

    @Resource
    private ServeDeployService serveDeployService;

    /**
     * Spring Boot 启动完成后启动 Serve 服务
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        serveDeployService.startServeService();
    }

    /**
     * Spring Boot 关闭时停止 Serve 服务
     */
    @PreDestroy
    public void onApplicationShutdown() {
        System.out.println("Shutting down Serve service...");
        serveDeployService.stopServeService();
    }
}
