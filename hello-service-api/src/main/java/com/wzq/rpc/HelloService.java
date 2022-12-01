package com.wzq.rpc;

/**
 * @author wzq
 * @create 2022-12-01 21:17
 */
public interface HelloService {

    /**
     * @param hello Hello实体类
     * @return 返回一个字符串
     */
    String hello(Hello hello);

}
