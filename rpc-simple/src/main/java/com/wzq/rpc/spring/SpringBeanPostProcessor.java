package com.wzq.rpc.spring;

import com.wzq.rpc.annotation.RpcService;
import com.wzq.rpc.entity.RpcServiceProperties;
import com.wzq.rpc.factory.SingletonFactory;
import com.wzq.rpc.provider.ServiceProvider;
import com.wzq.rpc.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 在创建每个bean之前，查看是否被标记@RpcService注解，如果包含该注解，则注册到zookeeper
 *
 * @author wzq
 * @create 2022-12-09 20:16
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;

    public SpringBeanPostProcessor() {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // 获取RpcService注解
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // 把RpcService中设置的version和group封装到RpcServiceProperties
            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .build();
            // 暴露服务
            serviceProvider.publishService(bean, rpcServiceProperties);
        }
        return bean;
    }
}
