package com.wj.springcloud.service;

import org.springframework.stereotype.Component;

@Component
public class PaymentFallbackService implements PaymentService{
    @Override
    public String paymentInfo_Ok(Integer id) {
        return "paymentInfo_Ok客户端服务降级处理";
    }

    @Override
    public String paymentInfo_Timeout(Integer id) {
        return "paymentInfo_Timeout客户端服务降级处理";
    }
}
