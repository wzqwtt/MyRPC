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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 操作Zookeeper工具类，使用Curator工具
 *
 * @author wzq
 * @create 2022-12-05 16:40
 */
@Slf4j
public class CuratorUtils {

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

    /**
     * 已注册服务的所有路径
     */
    private static Set<String> registeredPathSet = ConcurrentHashMap.newKeySet();

    private static CuratorFramework zkClient = getZKClient();

    /**
     * 防止其他人创建该类，构造方法私有化
     */
    private CuratorUtils() {
    }

    /**
     * 获取连接对象
     *
     * @return
     */
    private static CuratorFramework getZKClient() {
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
     * 创建永久节点，节点驻存在zookeeper中
     *
     * @param path 节点路径
     */
    public static void createPersistentNode(String path) {
        try {
            // 当registeredPathSet中存在路径或者zookeeper已经存在路径，则打印节点已存在
            if (registeredPathSet.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("节点[{}]已存在", path);
            } else {
                // eg: /my-rpc/com.wzq.rpc.HelloService/127.0.0.1:9999
                // 创建节点
                zkClient.create()
                        // 递归创建节点
                        .creatingParentsIfNeeded()
                        // 设置节点的Mode为永久节点
                        .withMode(CreateMode.PERSISTENT)
                        // 路径
                        .forPath(path);
                log.info("节点创建成功，节点为[{}]", path);
            }
            registeredPathSet.add(path);
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
        String servicePath = CuratorUtils.ZK_REGISTER_PORT_PATH + "/" + serviceName;

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
        String servicePath = CuratorUtils.ZK_REGISTER_PORT_PATH + "/" + serviceName;

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
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 清空注册中心的数据
     */
    public static void clearRegistry() {
        registeredPathSet.stream().parallel().forEach(p -> {
            try {
                zkClient.delete().forPath(p);
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e.getCause());
            }
        });
        log.info("服务端（Provider）所有注册的服务都被清空:[{}]", registeredPathSet.toString());
    }

}
