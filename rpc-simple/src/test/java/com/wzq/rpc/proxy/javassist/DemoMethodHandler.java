package com.wzq.rpc.proxy.javassist;

import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;

/**
 * @author wzq
 * @create 2023-01-12 21:34
 */
public class DemoMethodHandler implements MethodHandler {

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        System.out.println("----------Before selling fruits----------");
        // 执行代理的目标对象的方法
        Object result = proceed.invoke(self, args);
        System.out.println("----------After selling fruits----------");
        return result;
    }
}
