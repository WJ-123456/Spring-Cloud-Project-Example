package com.wj.springcloud.controller;

import com.wj.springcloud.entities.CommonResult;
import com.wj.springcloud.entities.Payment;
import com.wj.springcloud.service.Paymentservice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
public class PaymentController {
    @Resource
    private Paymentservice paymentservice;

    @Value("${server.port}")// 获取yml中配置属性
    private String serverPort;

    @PostMapping(value = "/payment/create")
    public CommonResult create(@RequestBody Payment payment){
        int result = paymentservice.create(payment);
        System.out.println("插入结果" + result);
        if (result > 0) {
            return new CommonResult(200,"数据新增成功！serverPort:" + serverPort,result);
        } else {
            return new CommonResult(444,"数据新增失败！serverPort:" + serverPort);
        }
    }

    @GetMapping(value = "/payment/get/{id}")
    public CommonResult getPaymentById(@PathVariable("id") Long id){
        Payment result = paymentservice.getPaymentById(id);
        System.out.println("据查询结果2" + result);
        if (result != null) {
            return new CommonResult(200,"数据查询成功！serverPort:" + serverPort,result);
        } else {
            return new CommonResult(444,"数据查询失败！serverPort:" + serverPort);
        }
    }

    @GetMapping(value = "/payment/lb")
    public String getPort(){
        return serverPort;
    }
}
