package com.wj.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OrderZooMain {
    public static void main(String[] args) {
        SpringApplication.run(OrderZooMain.class, args);
    }
}
