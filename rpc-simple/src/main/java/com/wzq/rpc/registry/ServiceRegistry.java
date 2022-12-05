package com.wzq.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册中心接口
 *
 * @author wzq
 * @create 2022-12-02 16:36
 */
public interface ServiceRegistry {

    /**
     * 注册服务
     *
     * @param serviceName       服务名称
     * @param inetSocketAddress socket地址
     */
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);

}
