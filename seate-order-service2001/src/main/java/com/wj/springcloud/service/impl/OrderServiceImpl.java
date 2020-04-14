package com.wj.springcloud.service.impl;

import com.wj.springcloud.Order;
import com.wj.springcloud.dao.OrderDao;
import com.wj.springcloud.service.AccountService;
import com.wj.springcloud.service.OrderService;
import com.wj.springcloud.service.StorageService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.xml.ws.RequestWrapper;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderDao orderDao;

    @Resource
    private StorageService storageService;

    @Resource
    private AccountService accountService;

    @Override
    @GlobalTransactional(name = "unionname",rollbackFor = Exception.class)// 发生任何异常都回退
    public void create(Order order) {
        // 1、订单的创建
        orderDao.create(order);
        // 库存扣减
        storageService.decrease(order.getProductId(), order.getCount());
        // 账户扣减
        accountService.decrease(order.getUserId(),order.getMoney());
        // 2、订单状态修改
        orderDao.update(order.getUserId(), 0);
    }
}
