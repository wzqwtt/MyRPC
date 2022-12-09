package com.wzq.rpc.provider;

import com.wzq.rpc.enumeration.RpcErrorMessage;
import com.wzq.rpc.exception.RpcException;
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
     * key: service/interface name
     * value: service
     */
    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_SERVICE = ConcurrentHashMap.newKeySet();

    private final ServiceRegistry serviceRegistry = new ZkServiceRegistry();

    /**
     * 注册一个service，将这个对象所有实现的接口都注册进去
     * <p>
     * TODO(scan) 修改为扫描注解注册
     *
     * @param service 服务
     */
    @Override
    public void addServiceProvider(Object service, Class<?> serviceClass) {
        // 获取service的名称，eg: com.wzq.rpc.HelloService
        String serviceName = serviceClass.getCanonicalName();

        // 如果传递过来的service所实现的接口已经被注册，那么直接返回
        if (REGISTERED_SERVICE.contains(serviceName)) {
            return;
        }

        REGISTERED_SERVICE.add(serviceName);

        SERVICE_MAP.put(serviceName, service);

        log.info("Add service: {} and interfaces {}", serviceName, service.getClass().getInterfaces());
    }

    /**
     * 获取指定接口名称的类
     *
     * @param serviceName 服务名称
     * @return 服务
     */
    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = SERVICE_MAP.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_FOUND);
        }
        return service;
    }

    /**
     * 发布服务
     *
     * @param service 服务实例对象
     */
    @Override
    public void publishService(Object service) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            Class<?> anInterface = service.getClass().getInterfaces()[0];
            this.addServiceProvider(service, anInterface);
            serviceRegistry.registerService(anInterface.getCanonicalName(), new InetSocketAddress(host, NettyServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
