package com.wj.springcloud.controller;

import com.wj.springcloud.Order;
import com.wj.springcloud.entities.CommonResult;
import com.wj.springcloud.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class OrderController {
    @Resource
    private OrderService orderService;

    @GetMapping(value = "/order/create")
    public CommonResult create(Order order){
        orderService.create(order);
        return new CommonResult(200,"success!");
    }
}
