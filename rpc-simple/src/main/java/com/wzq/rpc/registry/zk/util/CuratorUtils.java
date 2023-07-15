package com.wzq.rpc.registry.zk.util;

import com.wzq.rpc.enumeration.RpcConfigProperties;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.utils.file.PropertiesFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 操作Zookeeper工具类，使用Curator
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
    protected static final int MAX_RETRIES = 3;

    /**
     * zookeeper默认地址
     */
    private static String defaultZookeeperAddress = "127.0.0.1:2181";

    /**
     * MyRPC在zookeeper的根节点
     */
    public static final String ZK_REGISTER_PORT_PATH = "/my-rpc";

    /**
     * 服务地址
     */
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();

    /**
     * 已注册服务的所有路径
     */
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    /**
     * Zookeeper连接
     */
    private static CuratorFramework zkClient;

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
    public static CuratorFramework getZKClient() {
        // 检查用户是否配置了zookeeper地址
        Properties properties = PropertiesFileUtils.readPropertiesFile(RpcConfigProperties.RPC_CONFIG_PATH.getPropertyValue());

        // 如果properties不为空，就获取配置文件中zookeeper的值
        if (properties != null) {
            defaultZookeeperAddress = properties.getProperty(RpcConfigProperties.ZK_ADDRESS.getPropertyValue());
        }

        // 如果zkClient已经启动，那么直接返回
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }

        // 重试连接机制
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);

        zkClient = CuratorFrameworkFactory
                .builder()
                // 用于设置地址及端口号
                .connectString(defaultZookeeperAddress)
                // 用于设置重连策略
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        return zkClient;
    }

    /**
     * 创建永久节点，节点驻存在zookeeper中
     *
     * @param zkClient zookeeper连接
     * @param path     节点路径
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            // 当registeredPathSet中存在路径或者zookeeper已经存在路径，则打印节点已存在
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
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
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 获取某个service下的子节点，也就是获取所有提供服务的生产者的地址
     *
     * @param zkClient       zookeeper连接
     * @param rpcServiceName 服务名称
     * @return 返回子节点
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }

        List<String> result;
        String servicePath = CuratorUtils.ZK_REGISTER_PORT_PATH + "/" + rpcServiceName;

        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            // 注册监听
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }

        return result;
    }

    /**
     * 注册监听
     *
     * @param zkClient    zkClient
     * @param rpcServiceName 服务名称
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) {
        String servicePath = CuratorUtils.ZK_REGISTER_PORT_PATH + "/" + rpcServiceName;

        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        pathChildrenCache.getListenable()
                .addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                        List<String> serviceAddresses = client.getChildren().forPath(servicePath);
                        SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
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
     *
     * @param zkClient zookeeper连接
     */
    public static void clearRegistry(CuratorFramework zkClient) {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                zkClient.delete().forPath(p);
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e.getCause());
            }
        });
        log.info("服务端（Provider）所有注册的服务都被清空:[{}]", REGISTERED_PATH_SET.toString());
    }

}
