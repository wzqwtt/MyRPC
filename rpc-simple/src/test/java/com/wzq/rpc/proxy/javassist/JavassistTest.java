package com.wzq.rpc.proxy.javassist;

import com.wzq.rpc.proxy.common.FruitGrower;
import com.wzq.rpc.proxy.common.Sales;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.Method;

/**
 * @author wzq
 * @create 2023-01-12 21:35
 */
public class JavassistTest {

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        ProxyFactory proxyFactory = new ProxyFactory();
        // 设置被代理类
        proxyFactory.setSuperclass(FruitGrower.class);
        // 设置方法过滤器
        proxyFactory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(Method method) {
                return !"finalize".equals(method.getName());
            }
        });
        // 创建代理类
        Class<?> c = proxyFactory.createClass();
        Sales sales = (Sales) c.newInstance();
        // 设置方法调用处理器
        ((Proxy) sales).setHandler(new DemoMethodHandler());
        // 调用方法
        sales.sellFruit();
    }

}
