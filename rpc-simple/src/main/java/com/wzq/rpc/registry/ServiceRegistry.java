package com.wzq.rpc.registry;

/**
 * 服务注册中心接口
 *
 * @author wzq
 * @create 2022-12-02 16:36
 */
public interface ServiceRegistry {

    /**
     * 注册一个服务
     *
     * @param service 服务
     * @param <T>
     */
    <T> void register(T service);

    /**
     * 获取一个服务
     *
     * @param serviceName 服务名称
     * @return
     */
    Object getService(String serviceName);

}
