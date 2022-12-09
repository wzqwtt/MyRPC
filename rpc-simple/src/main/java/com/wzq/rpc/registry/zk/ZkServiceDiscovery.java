package com.wzq.rpc.registry.zk;

import com.wzq.rpc.loadbalance.LoadBalance;
import com.wzq.rpc.loadbalance.RandomLoadBalance;
import com.wzq.rpc.registry.ServiceDiscovery;
import com.wzq.rpc.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 基于Zookeeper的服务发现
 *
 * @author wzq
 * @create 2022-12-05 22:04
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public ZkServiceDiscovery() {
        // TODO(balance): 添加更多负载均衡算法
        this.loadBalance = new RandomLoadBalance();
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // 获取zookeeper连接
        CuratorFramework zkClient = CuratorUtils.getZKClient();
        // 找到该服务的所有服务地址
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, serviceName);

        // 使用负载均衡算法找到一个服务地址
        // eg: 127.0.0.1:9999
        String targetServiceAddress = loadBalance.selectServiceAddress(serviceUrlList);
        log.info("成功找到服务地址: [{}]", targetServiceAddress);
        String[] address = targetServiceAddress.split(":");

        return new InetSocketAddress(address[0], Integer.parseInt(address[1]));
    }
}
