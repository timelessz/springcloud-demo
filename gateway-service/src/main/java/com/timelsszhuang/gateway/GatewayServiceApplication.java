package com.timelsszhuang.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Gateway 服务启动类
 *
 * @author timelsszhuang
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("Gateway Service 启动成功!");
        System.out.println("访问地址: http://localhost:8085");
        System.out.println("========================================\n");
    }

}

