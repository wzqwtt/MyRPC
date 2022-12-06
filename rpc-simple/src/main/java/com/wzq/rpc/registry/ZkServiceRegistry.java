package com.wzq.rpc.registry;

import com.wzq.rpc.utils.zk.CuratorHelper;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 基于zookeeper实现注册中心
 *
 * @author wzq
 * @create 2022-12-05 19:24
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {
    
    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        // 根节点下注册子节点：服务
        StringBuilder servicePath = new StringBuilder(CuratorHelper.ZK_REGISTER_PORT_PATH).append("/").append(serviceName);
        // 服务子节点下注册子节点：服务地址
        // 节点路径: /my-rpc/com.wzq.rpc.HelloService/127.0.0.1:9999
        servicePath.append(inetSocketAddress.toString());
        // 注册为临时节点，Server关闭即释放实现类信息
        CuratorHelper.createEphemeraNode(servicePath.toString());
    }

}
