package com.wj.test;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;

import javax.annotation.PostConstruct;

public class IdGeneratorSnow {
    private long workid = 0;
    private Snowflake snowflake = IdUtil.createSnowflake(workid,1);

    @PostConstruct
    public void init(){
        try {
            workid = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
            System.out.println("当前机器ID" + workid);
        } catch (Exception e) {
            System.out.println("发生异常");
            workid = NetUtil.getLocalhostStr().hashCode();
        } finally {

        }
    }

    public synchronized long snowflakId(){
        return snowflake.nextId();
    }

    public synchronized long snowflakId(long workid, long datacenterId){
        Snowflake snowflake = IdUtil.createSnowflake(workid,datacenterId);
        return snowflake.nextId();
    }

    public static void main(String[] args) {
        System.out.println(new IdGeneratorSnow().snowflakId());
    }
}
