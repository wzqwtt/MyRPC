package com.wzq.rpc.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author wzq
 * @create 2023-01-12 21:10
 */
public class DemoInvocationHandler implements InvocationHandler {

    /**
     * 需要代理的目标对象
     */
    private Object target;

    public DemoInvocationHandler(Object target) {
        this.target = target;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        System.out.println("----------Before selling fruits----------");
        // 执行代理的目标对象的方法
        Object result = method.invoke(target, args);
        System.out.println("----------After selling fruits----------");
        return result;
    }
}
