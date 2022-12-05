package com.wzq.rpc.registry;

import com.wzq.rpc.utils.zk.CuratorHelper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 基于zookeeper实现注册中心
 *
 * @author wzq
 * @create 2022-12-05 19:24
 */
public class ZkServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistry.class);

    /**
     * zookeeper客户端
     */
    private final CuratorFramework zkClient;

    /**
     * 获取zkClient，并启动zk客户端
     */
    public ZkServiceRegistry() {
        // 获取ZkClient
        zkClient = CuratorHelper.getZKClient();
        // 启动zk客户端
        zkClient.start();
    }

    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        // 根节点下注册子节点：服务
        StringBuilder servicePath = new StringBuilder(CuratorHelper.ZK_REGISTER_PORT_PATH).append("/").append(serviceName);
        // 服务子节点下注册子节点：服务地址
        // 节点路径: /my-rpc/com.wzq.rpc.HelloService/127.0.0.1:9999
        servicePath.append(inetSocketAddress.toString());
        // 注册为临时节点，Server关闭即释放实现类信息
        CuratorHelper.createEphemeraNode(zkClient, servicePath.toString());
        logger.info("节点创建成功，节点为:{}", servicePath);
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        String serviceAddress = CuratorHelper.getChildrenNodes(zkClient, serviceName).get(0);
        logger.info("成功找到服务地址:{}", serviceAddress);
        String[] address = serviceAddress.split(":");
        return new InetSocketAddress(address[0], Integer.parseInt(address[1]));
    }
}
