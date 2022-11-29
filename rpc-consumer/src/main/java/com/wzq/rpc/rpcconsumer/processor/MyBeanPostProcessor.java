package com.wzq.rpc.rpcconsumer.processor;

import com.wzq.rpc.rpcconsumer.anno.RpcReference;
import com.wzq.rpc.rpcconsumer.proxy.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * bean的后置增强
 *
 * @author wzq
 * @create 2022-11-29 20:10
 */
@Slf4j
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    RpcClientProxy rpcClientProxy;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 查看bean的字段中有没有对应注解
        Field[] declaredFields = bean.getClass().getDeclaredFields();

        log.debug("debug: {}", bean.getClass());

        for (Field field : declaredFields) {
            // 查找字段中是否包含这个注解
            RpcReference annotation = field.getAnnotation(RpcReference.class);

            if (annotation != null) {
                // 获取代理对象
                Object proxy = rpcClientProxy.getProxy(field.getType());

                try {
                    // 属性注入
                    field.setAccessible(true);
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
