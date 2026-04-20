package com.diaoji;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 钓迹 App - 后端服务启动类
 */
@SpringBootApplication
@EnableScheduling
public class DiaojiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiaojiApplication.class, args);
        System.out.println("🎣 钓迹 App 服务已启动！");
    }
}
