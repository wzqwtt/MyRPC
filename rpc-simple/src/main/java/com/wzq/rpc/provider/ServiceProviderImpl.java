package com.wzq.rpc.provider;

import com.wzq.rpc.entity.RpcServiceProperties;
import com.wzq.rpc.enumeration.RpcErrorMessage;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.extension.ExtensionLoader;
import com.wzq.rpc.registry.ServiceRegistry;
import com.wzq.rpc.registry.zk.ZkServiceRegistry;
import com.wzq.rpc.remoting.transport.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的ServiceRegistry实现，通过Map保存服务信息
 *
 * @author wzq
 * @create 2022-12-02 16:39
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    /**
     * 接口名和服务的对应关系
     * <p>
     * key: service name (eg: interface name + version + group)
     * <p>
     * value: service object
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;

    private final ServiceRegistry serviceRegistry;

    public ServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        // 通过SPI机制获取服务注册中心对象
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    /**
     * 注册一个service，将这个对象所有实现的接口都注册进去
     *
     * @param service 服务
     */
    @Override
    public void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties) {
        // service name从属性中获取
        String rpcServiceName = rpcServiceProperties.toRpcServiceName();

        // 如果该service name已经被注册，那么直接返回
        if (registeredService.contains(rpcServiceName)) {
            return;
        }

        registeredService.add(rpcServiceName);

        serviceMap.put(rpcServiceName, service);

        log.info("Add service: {} and interfaces {}", rpcServiceName, service.getClass().getInterfaces());
    }

    /**
     * 获取指定接口名称的类
     *
     * @param rpcServiceProperties 服务相关联的属性
     * @return 服务
     */
    @Override
    public Object getService(RpcServiceProperties rpcServiceProperties) {
        String serviceName = rpcServiceProperties.toRpcServiceName();
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_FOUND);
        }
        return service;
    }

    /**
     * 发布服务
     *
     * @param service              服务实例对象
     * @param rpcServiceProperties 服务相关的属性
     */
    @Override
    public void publishService(Object service, RpcServiceProperties rpcServiceProperties) {
        try {
            // 获取主机IP
            String host = InetAddress.getLocalHost().getHostAddress();
            // 获取该服务实现类实现的第一个接口
            Class<?> serviceRelatedInterface = service.getClass().getInterfaces()[0];

            // 获取该接口的全类名，然后设置到ServiceName中
            String serviceName = serviceRelatedInterface.getCanonicalName();
            rpcServiceProperties.setServiceName(serviceName);

            // 直接添加到Provider
            this.addService(service, serviceRelatedInterface, rpcServiceProperties);
            // 将该服务直接注册到注册中心
            serviceRegistry.registerService(rpcServiceProperties.toRpcServiceName(), new InetSocketAddress(host, NettyServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }

    /**
     * 发布服务
     *
     * @param service 服务实例对象
     */
    @Override
    public void publishService(Object service) {
        this.publishService(service, RpcServiceProperties.builder().version("").group("").build());
    }
}
