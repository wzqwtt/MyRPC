package com.wzq.rpc.proxy.common;

/**
 * 果农
 *
 * @author wzq
 * @create 2023-01-12 21:14
 */
public class FruitGrower implements Sales {
    @Override
    public void sellFruit() {
        System.out.println("Successfully sold fruits.");
    }
}
