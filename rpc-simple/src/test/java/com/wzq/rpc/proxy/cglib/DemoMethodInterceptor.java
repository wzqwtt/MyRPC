package com.wzq.rpc.proxy.cglib;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author wzq
 * @create 2023-01-12 21:27
 */
public class DemoMethodInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("----------Before selling fruits----------");
        // 执行代理的目标对象的方法
        Object result = methodProxy.invokeSuper(o, objects);
        System.out.println("----------After selling fruits----------");
        return result;
    }
}
