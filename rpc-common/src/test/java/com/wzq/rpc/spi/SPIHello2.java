package com.wzq.rpc.spi;

/**
 * @author wzq
 * @create 2022-12-13 19:24
 */
public class SPIHello2 implements SPIInterfaceDemo {
    @Override
    public void sayHello(String msg) {
        System.out.println("SPIHello2 " + msg);
    }
}
