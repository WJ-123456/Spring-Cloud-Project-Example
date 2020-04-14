package com.wj.spring.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CircleBreakController {
    public static final String SERVICE_URL = "http://nacos-payment-provider";

    @RequestMapping("/consumer/fallback/{id}")
    @SentinelResource(value = "fallback",
            fallback = "deal_runException",
            exceptionsToIgnore = {IllegalAccessException.class}) // 业务异常处理
    public String fallBack(@PathVariable Long id){
        int i = 10/0; // 执行到此处会，会调用deal_runException方法，如果没有配置fallback则返回异常界面
        return "";
    }
    public String deal_runException(@PathVariable Long id, Throwable e){
        return "deal_runException" + e.getMessage();
    }
}
