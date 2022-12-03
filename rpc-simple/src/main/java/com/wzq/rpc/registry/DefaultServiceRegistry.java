package com.wzq.rpc.registry;

import com.wzq.rpc.enumeration.RpcErrorMessageEnum;
import com.wzq.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的ServiceRegistry实现
 *
 * @author wzq
 * @create 2022-12-02 16:39
 */
public class DefaultServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceRegistry.class);

    /**
     * 接口名和服务的对应关系，TODO 处理一个接口被两个实现类实现的情况
     * key: service/interface name
     * value: service
     */
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 注册一个service，将这个对象所有实现的接口都注册进去
     * <p>
     * TODO 修改为扫描注解注册
     *
     * @param service 服务
     * @param <T>
     */
    @Override
    public <T> void register(T service) {
        // 获取service的名称
        String serviceName = service.getClass().getCanonicalName();

        // 如果传递过来的service所实现的接口已经被注册，那么直接返回
        if (registeredService.contains(serviceName)) {
            return;
        }

        registeredService.add(serviceName);

        // 获取传递过来的service实现的所有接口
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            // 如果没有实现任何接口，那么throw一个异常
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }

        // 所有接口都注册到map中
        for (Class<?> i : interfaces) {
            serviceMap.put(i.getCanonicalName(), service);
        }

        logger.info("Add service: {} and interfaces {}", serviceName, interfaces);
    }

    /**
     * 获取指定接口名称的类
     *
     * @param serviceName 服务名称
     * @return 服务
     */
    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_FOUND);
        }
        return service;
    }
}
