package com.wj.springcloud.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wj.springcloud"})
public class MybatisConfig {
    //Mybatis扫描实体类配置
}
