package com.wj.springcloud.service;

import com.wj.springcloud.entities.Payment;
import org.apache.ibatis.annotations.Param;

public interface Paymentservice {
    public int create(Payment payment);

    public Payment getPaymentById(@Param("id") Long id);
}
