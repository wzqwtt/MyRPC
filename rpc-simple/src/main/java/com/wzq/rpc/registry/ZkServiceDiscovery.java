package com.wzq.rpc.registry;

import com.wzq.rpc.utils.zk.CuratorHelper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 基于Zookeeper的服务发现
 *
 * @author wzq
 * @create 2022-12-05 22:04
 */
public class ZkServiceDiscovery implements ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceDiscovery.class);

    /**
     * zookeeper客户端
     */
    private final CuratorFramework zkClient;

    /**
     * 获取zkClient，并启动zk客户端
     */
    public ZkServiceDiscovery() {
        // 获取ZkClient
        zkClient = CuratorHelper.getZKClient();
        // 启动zk客户端
        zkClient.start();
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // TODO(blance) 负载均衡
        // 这里直接取了第一个找到的服务地址
        String serviceAddress = CuratorHelper.getChildrenNodes(zkClient, serviceName).get(0);
        logger.info("成功找到服务地址:{}", serviceAddress);
        String[] address = serviceAddress.split(":");
        return new InetSocketAddress(address[0], Integer.parseInt(address[1]));
    }
}
