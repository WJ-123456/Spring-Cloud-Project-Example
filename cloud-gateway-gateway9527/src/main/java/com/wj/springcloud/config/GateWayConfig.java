package com.wj.springcloud.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GateWayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder){
        RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();
        builder.route("path_route3",    // 路由的id
                r -> r.path("com") // 访问路径
                        .uri("http://www.baidu,com")    // 跳转路径
        ).build();
        return builder.build();
    }
    // 如果还有地址，需要新建一个方法返回RouteLocator
}
