package com.wj.springcloud.service;

import com.wj.springcloud.entities.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 仓库应用
@FeignClient(value = "seata-storage-service")
public interface StorageService {
    @PostMapping(value = "/storage/decrease") // 操作数据库用POST
    CommonResult decrease(@RequestParam("productId")Long productId,
                          @RequestParam("count") Integer count);
}
