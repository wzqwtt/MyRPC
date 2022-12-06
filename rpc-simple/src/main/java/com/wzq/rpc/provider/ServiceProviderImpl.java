package com.wzq.rpc.provider;

import com.wzq.rpc.enumeration.RpcErrorMessageEnum;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的ServiceRegistry实现，通过Map保存服务信息
 *
 * @author wzq
 * @create 2022-12-02 16:39
 */
public class ServiceProviderImpl implements ServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderImpl.class);

    /**
     * 接口名和服务的对应关系
     * key: service/interface name
     * value: service
     */
    private static Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private static Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 注册一个service，将这个对象所有实现的接口都注册进去
     * <p>
     * TODO(scan) 修改为扫描注解注册
     *
     * @param service 服务
     * @param <T>
     */
    @Override
    public <T> void addServiceProvider(T service, Class<T> serviceClass) {
        // 获取service的名称
        String serviceName = serviceClass.getCanonicalName();

        // 如果传递过来的service所实现的接口已经被注册，那么直接返回
        if (registeredService.contains(serviceName)) {
            return;
        }

        registeredService.add(serviceName);

        serviceMap.put(serviceName, service);

        logger.info("Add service: {} and interfaces {}", serviceName, service.getClass().getInterfaces());
    }

    /**
     * 获取指定接口名称的类
     *
     * @param serviceName 服务名称
     * @return 服务
     */
    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_FOUND);
        }
        return service;
    }
}
