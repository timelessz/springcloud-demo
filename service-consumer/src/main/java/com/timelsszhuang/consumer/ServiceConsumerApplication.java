package com.timelsszhuang.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceConsumerApplication.class, args);
    }

    /**
     * 带负载均衡的 RestTemplate，用于通过服务名调用（如 http://service-provider/...）
     */
    @Bean("loadBalancedRestTemplate")
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }

    /**
     * 普通 RestTemplate，用于直接调用 IP:PORT（如 http://127.0.0.1:8085/...）
     */
    @Bean("plainRestTemplate")
    public RestTemplate plainRestTemplate() {
        return new RestTemplate();
    }

}

