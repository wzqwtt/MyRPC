package com.wzq.rpc.proxy.cglib;

import com.wzq.rpc.proxy.common.FruitGrower;
import com.wzq.rpc.proxy.common.Sales;
import net.sf.cglib.proxy.Enhancer;

/**
 * @author wzq
 * @create 2023-01-12 21:28
 */
public class CGLibProxyTest {

    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(FruitGrower.class);
        enhancer.setCallback(new DemoMethodInterceptor());

        Sales proxy = (Sales) enhancer.create();
        proxy.sellFruit();
    }

}
