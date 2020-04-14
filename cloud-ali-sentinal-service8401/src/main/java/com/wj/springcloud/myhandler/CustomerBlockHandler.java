package com.wj.springcloud.myhandler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.wj.springcloud.entities.CommonResult;

// 自定义全局兜底类
public class CustomerBlockHandler {
    public static CommonResult handlerException(BlockException e){
        return new CommonResult(444,"全局处理方法！");
    }
}
