package com.wzq.rpc.proxy.jdk;

import com.wzq.rpc.proxy.common.FruitGrower;
import com.wzq.rpc.proxy.common.Sales;

import java.lang.reflect.Proxy;

/**
 * @author wzq
 * @create 2023-01-12 21:13
 */
public class JDKProxyTest {

    public static void main(String[] args) {
        FruitGrower fruitGrower = new FruitGrower();
        ClassLoader classLoader = fruitGrower.getClass().getClassLoader();
        Class<?>[] interfaces = fruitGrower.getClass().getInterfaces();

        DemoInvocationHandler invocationHandler = new DemoInvocationHandler(fruitGrower);

        Sales proxy = (Sales) Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
        proxy.sellFruit();
    }

}
