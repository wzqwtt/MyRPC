package com.wzq.rpc.registry;

import com.wzq.rpc.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务注册中心接口
 *
 * @author wzq
 * @create 2022-12-02 16:36
 */
@SPI
public interface ServiceRegistry {

    /**
     * 注册服务
     *
     * @param rpcServiceName    服务名称
     * @param inetSocketAddress socket地址
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
