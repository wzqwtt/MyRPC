package com.wzq.rpc.registry;

import com.wzq.rpc.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 *
 * @author wzq
 * @create 2022-12-05 22:03
 */
@SPI
public interface ServiceDiscovery {

    /**
     * 查找服务
     *
     * @param rpcServiceName 服务名称
     * @return 返回服务的socket地址
     */
    InetSocketAddress lookupService(String rpcServiceName);

}
