package com.wzq.rpc.provider;

/**
 * 保存和提供服务实例对象。服务端使用
 *
 * @author wzq
 * @create 2022-12-05 14:28
 */
public interface ServiceProvider {

    /**
     * 保存服务提供者
     *
     * @param service service
     * @param <T>     service的类型
     */
    <T> void addServiceProvider(T service);

    /**
     * 获取服务提供者
     *
     * @param serviceName serviceName
     * @return 服务实例对象
     */
    Object getServiceProvider(String serviceName);
}
