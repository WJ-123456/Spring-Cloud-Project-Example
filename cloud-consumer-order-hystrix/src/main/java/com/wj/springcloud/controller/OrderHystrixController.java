package com.wj.springcloud.controller;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.wj.springcloud.service.PaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RestController
@DefaultProperties(defaultFallback = "payment_global_fallback")
public class OrderHystrixController {
    @Resource
    public PaymentService paymentService;

    @GetMapping(value = "/consumer/payment/hystrix/ok/{id}")
    @HystrixCommand // 使用全局紧急方法
    public String paymentInfo_Ok(@PathVariable("id") Integer id){
        return paymentService.paymentInfo_Ok(id);
    }


    @GetMapping(value = "/consumer/payment/hystrix/timeout/{id}")
    /*@HystrixCommand(fallbackMethod = "paymentInfo_TimeoutHandler",commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1500")
    })*/ // 使用自身指定的紧急方法，该方式与全局指定降级方法会产生冲突
    @HystrixCommand
    public String paymentInfo_Timeout(@PathVariable("id")Integer id){
        return paymentService.paymentInfo_Timeout(id);
    }

    public String paymentInfo_TimeoutHandler(@PathVariable("id") Integer id){
        return "客户端紧急处理!";
    }

    public String payment_global_fallback(@PathVariable("id") Integer id){
        return "全局紧急处理!";
    }
}
