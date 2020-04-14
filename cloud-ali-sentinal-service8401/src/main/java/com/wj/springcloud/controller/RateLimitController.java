package com.wj.springcloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.wj.springcloud.entities.CommonResult;
import com.wj.springcloud.entities.Payment;
import com.wj.springcloud.myhandler.CustomerBlockHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimitController {
    @GetMapping("/byResource")
    @SentinelResource(value = "byResource",blockHandler = "handleException")
    public CommonResult byResource(){
        Payment payment = new Payment();
        return new CommonResult(200,"测试ok",payment);
    }

    public CommonResult handleException(BlockException e){
        return new CommonResult(404,e.getClass().getCanonicalName()+" 服务不可用！");
    }

    @GetMapping("/rateLimit/byUrl")
    @SentinelResource(value = "byUrl")// 会调用系统默认自带的兜底方法处理
    public CommonResult byUrl(){
        return new CommonResult(200,"byUrl测试ok");
    }

    @GetMapping("/rateLimit/customerBlockHandler")
    @SentinelResource(value = "customerBlockHandler",
            blockHandlerClass = CustomerBlockHandler.class,
            blockHandler = "handlerException")// 会调用CustomerBlockHandler类的handlerException方法处理
    public CommonResult customerBlockHandler(){
        return new CommonResult(200,"byUrl测试ok");
    }

}
