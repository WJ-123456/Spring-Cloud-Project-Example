package com.wj.springcloud.controller;

import com.wj.springcloud.entities.CommonResult;
import com.wj.springcloud.entities.Payment;
import com.wj.springcloud.service.Paymentservice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class PaymentController {
    @Resource
    private Paymentservice paymentservice;

    @Value("${server.port}")
    private String serverPort;

    @Resource
    private DiscoveryClient discoveryClient;

    // 将该服务在Eureka服务注册的信息暴露给Eureka的客户客户端
    @GetMapping(value = "/payment/discovery")
    public Object getEurekaServerInfo(){
        // 获取服务名称
        List<String> services = discoveryClient.getServices();
        // 获取服务实例信息，包含服务id,服务器名称，端口，访问uri
        List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
        return this.discoveryClient;
    }

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

    @GetMapping(value = "/payment/feign/timeout")
    public String getTimeOut(){
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return serverPort;
    }

    @GetMapping(value = "/payment/zipkin")
    public String paymentZipkin(){
        System.out.println("zip 测试");
        return "zipkin 测试！";
    }

}
