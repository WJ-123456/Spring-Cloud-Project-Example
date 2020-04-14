package com.wj.springcloud.service;

import com.wj.springcloud.entities.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

// 账户应用
@FeignClient(value = "seata-account-service")
public interface AccountService {
    @PostMapping(value = "/account/decrease") // 操作数据库用POST
    CommonResult decrease(@RequestParam("userId")Long userId,
                          @RequestParam("money") BigDecimal money);
}
