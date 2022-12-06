package com.wzq.rpc.registry;

import com.wzq.rpc.utils.zk.CuratorHelper;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 基于Zookeeper的服务发现
 *
 * @author wzq
 * @create 2022-12-05 22:04
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // TODO(blance) 负载均衡
        // 这里直接取了第一个找到的服务地址
        String serviceAddress = CuratorHelper.getChildrenNodes(serviceName).get(0);
        log.info("成功找到服务地址:{}", serviceAddress);
        String[] address = serviceAddress.split(":");
        return new InetSocketAddress(address[0], Integer.parseInt(address[1]));
    }
}
