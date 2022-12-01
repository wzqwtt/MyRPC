package com.wzq.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wzq
 * @create 2022-12-01 22:43
 */
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(Hello hello) {
        logger.info("HelloServiceImpl收到: {}", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        logger.info("HelloServiceImpl返回: {}", result);
        return result;
    }
}
