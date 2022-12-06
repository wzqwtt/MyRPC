package com.wzq.rpc.utils.zk;

import com.wzq.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 操作Zookeeper工具类，使用Curator工具
 *
 * @author wzq
 * @create 2022-12-05 16:40
 */
@Slf4j
public class CuratorHelper {
    
    /**
     * 连接重试间隔
     */
    private static final int BASE_SLEEP_TIME = 100;

    /**
     * 重试连接次数
     */
    protected static final int MAX_RETRIES = 5;

    /**
     * zookeeper服务端地址
     */
    private static final String CONNECT_STRING = "127.0.0.1:2181";

    /**
     * MyRPC在zookeeper的根节点
     */
    public static final String ZK_REGISTER_PORT_PATH = "/my-rpc";

    /**
     * 服务地址
     */
    private static Map<String, List<String>> serviceAddressMap = new ConcurrentHashMap<>();

    private static CuratorFramework zkClient = getZKClient();

    /**
     * 防止其他人创建该类，构造方法私有化
     */
    private CuratorHelper() {
    }

    /**
     * 获取连接对象
     *
     * @return
     */
    public static CuratorFramework getZKClient() {
        // 重试连接机制
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);

        CuratorFramework curatorFramework = CuratorFrameworkFactory
                .builder()
                // 用于设置地址及端口号
                .connectString(CONNECT_STRING)
                // 用于设置重连策略
                .retryPolicy(retryPolicy)
                .build();
        curatorFramework.start();
        return curatorFramework;
    }

    /**
     * 创建临时节点，临时节点驻存在zookeeper种，当链接和session断掉时被删除
     *
     * @param path     节点路径
     */
    public static void createEphemeraNode(String path) {
        try {
            if (zkClient.checkExists().forPath(path) == null) {
                // 创建临时节点
                zkClient.create()
                        // 递归创建节点
                        .creatingParentsIfNeeded()
                        // 设置节点的Mode为临时节点
                        .withMode(CreateMode.EPHEMERAL)
                        // 路径
                        .forPath(path);
                log.info("节点创建成功，节点为[{}]", path);
            } else {
                log.info("节点[{}]已存在", path);
            }
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 获取某个service下的子节点，也就是获取所有提供服务的生产者的地址
     *
     * @param serviceName 服务名称
     * @return 返回子节点
     */
    public static List<String> getChildrenNodes(String serviceName) {
        if (serviceAddressMap.containsKey(serviceName)) {
            return serviceAddressMap.get(serviceName);
        }

        List<String> result;
        String servicePath = CuratorHelper.ZK_REGISTER_PORT_PATH + "/" + serviceName;

        try {
            result = zkClient.getChildren().forPath(servicePath);
            serviceAddressMap.put(serviceName, result);
            // 注册监听
            registerWatcher(zkClient, serviceName);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }

        return result;
    }

    /**
     * 注册监听
     *
     * @param zkClient    zkClient
     * @param serviceName 服务名称
     */
    private static void registerWatcher(CuratorFramework zkClient, String serviceName) {
        String servicePath = CuratorHelper.ZK_REGISTER_PORT_PATH + "/" + serviceName;

        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        pathChildrenCache.getListenable()
                .addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                        List<String> serviceAddresses = client.getChildren().forPath(servicePath);
                        serviceAddressMap.put(serviceName, serviceAddresses);
                    }
                });

        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            log.error("occur exception:", e);
        }
    }

}
