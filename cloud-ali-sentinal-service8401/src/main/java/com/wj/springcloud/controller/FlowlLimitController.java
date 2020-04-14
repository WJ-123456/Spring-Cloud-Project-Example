package com.wj.springcloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FlowlLimitController {
    @GetMapping("/testA")
    public String testA(){
       return "--- test a";
    }

    @GetMapping("/testB")
    public String testB(){
        return "--- test b";
    }

    @GetMapping("/testHotKey")
    @SentinelResource(value = "testHotKey", blockHandler = "deal_testHotKey")// 兜底处理方法
    public String testHotKey(@RequestParam(value = "p1",required = false)String p1,
                             @RequestParam(value = "p2",required = false)String p2){
        return "---- testHotKey";
    }

    public String deal_testHotKey(String p1, String p2, BlockException e){
        return "---- deal_testHotKey,Is final Method!";
    }
}
