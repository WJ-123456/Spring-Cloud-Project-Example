package com.wj.springcloud.service.impl;

import com.wj.springcloud.dao.PaymentDao;
import com.wj.springcloud.entities.Payment;
import com.wj.springcloud.service.Paymentservice;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PaymentserviceImpl implements Paymentservice {
    @Resource
    private PaymentDao paymentDao;

    @Override
    public int create(Payment payment) {
        return paymentDao.create(payment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentDao.getPaymentById(id);
    }
}
