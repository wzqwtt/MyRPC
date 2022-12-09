package com.wzq.rpc.service;

import com.wzq.rpc.Hello;
import com.wzq.rpc.HelloService;
import com.wzq.rpc.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wzq
 * @create 2022-12-01 22:43
 */
@Slf4j
@RpcService
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("被注册了");
    }
    
    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl收到: {}", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}", result);
        return result;
    }
}
