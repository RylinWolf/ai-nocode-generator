package com.wolfhouse.yuaicodemother.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linexsong
 */
@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping("/")
    public String health() {
        return "ok";
    }
}
