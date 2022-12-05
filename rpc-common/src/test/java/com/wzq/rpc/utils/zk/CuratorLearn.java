package com.wzq.rpc.utils.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * curator框架在zookeeper原生API接口上进行了包装，解决了很多zooKeeper客户端非常底层的细节开发。
 * <p>
 * 提供zooKeeper各种应用场景(比如：分布式锁服务、集群领导选举、 共享计数器、缓存机制、分布式队列等)的抽象封装，实现了Fluent风格的API接口，是最好用，最流行的zookeeper的客户端。
 *
 * @author wzq
 * @create 2022-12-05 15:04
 */
public class CuratorLearn {

    private static final Logger logger = LoggerFactory.getLogger(CuratorLearn.class);

    private CuratorFramework client;
    private final String connectString = "localhost:2181";
    private final int sessionTimeoutMs = 5000;
    private final int baseSleepTimeMs = 1000;
    private final int maxRetries = 3;
    private final String namespace = "learn";

    /**
     * 测试使用Curator对zookeeper进行连接和关闭
     */
    @Before
    public void connectZK() {
        // 表示间隔1s，最多尝试
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries);

        // 采用了工厂设计模式和建造者模式。通过输入一些连接信息，可以获取一个连接Zookeeper服务器的客户端
        client = CuratorFrameworkFactory
                .builder()
                // 用于设置地址及端口号
                .connectString(connectString)
                // 用于设置超时时间
                .sessionTimeoutMs(sessionTimeoutMs)
                // 用于设置重连策略
                .retryPolicy(retryPolicy)
                // 表示根节点路径，可以没有
                .namespace(namespace)
                .build();

        // 开启客户端
        client.start();
        logger.info("client state: {}", client.getState());
    }

    @After
    public void closeZK() {
        // 关闭客户端
        client.close();
        logger.info("client state: {}", client.getState());
    }

    /**
     * 简单创建
     */
    @Test
    public void testCreat1() throws Exception {
        client.create()
                // CreateMode value determines how the znode is created on ZooKeeper.
                .withMode(CreateMode.PERSISTENT)
                // This Id represents anyone.
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                // 在namespace节点下，创建节点node，存入的数据是"test/node1"的字节数组
                .forPath("/node1", "test/node1".getBytes());
        logger.info("/test/node1被创建");
    }

    /**
     * 自定义权限创建
     */
    @Test
    public void testCreate2() throws Exception {
        Id ip = new Id("ip", "127.0.0.1");
        List<ACL> acl = Collections.singletonList(new ACL(ZooDefs.Perms.ALL, ip));
        client.create()
                .withMode(CreateMode.PERSISTENT)
                .withACL(acl)
                .forPath("/node2", "test/node2".getBytes());
        logger.info("/test/node2被创建");
    }

    /**
     * 递归创建节点
     */
    @Test
    public void testCreate3() throws Exception {
        client.create()
                // 递归创建节点
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath("/node3/subnode", "node3/subnode".getBytes());
        logger.info("/node3/subnode被创建");
    }

    /**
     * 异步方法创建节点
     */
    @Test
    public void testCreate4() throws Exception {
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                // 异步方法创建节点
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                        // true，此处的curatorFramework即为之前创建的client
                        logger.info("curatorFramework == client : {}", curatorFramework == client);
                        // 0表示创建成功
                        logger.info("getResultCode(): {}", curatorEvent.getResultCode());
                        // 获取操作类型 CREATE
                        logger.info("getType(): {}", curatorEvent.getType().toString());
                        // 获取节点路径
                        logger.info("getPath(): {}", curatorEvent.getPath());
                    }
                })
                .forPath("/node4/subnote", "/node4/subnote".getBytes());

        // 在main线程打印一些内容
        for (int i = 0; i < 10; i++) {
            logger.info("计数: {}", i);
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新一个节点
     */
    @Test
    public void testUpdate1() throws Exception {
        client.setData()
                .forPath("/node1", "update node1".getBytes());
        logger.info("更新{}/node1", namespace);
    }

    /**
     * 带版本更新一个节点
     */
    @Test
    public void testUpdate2() throws Exception {
        client.setData()
                // 带有版本号
                .withVersion(1)
                .forPath("/node1", "testUpdate2".getBytes());
        logger.info("带有版本号更新{}/node1", namespace);
    }

    /**
     * 带有回调方法更新节点
     *
     * @throws Exception
     */
    @Test
    public void testUpdate3() throws Exception {
        client.setData()
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                        // true，此处的curatorFramework即为之前创建的client
                        logger.info("curatorFramework == client : {}", curatorFramework == client);
                        // 0表示创建成功
                        logger.info("getResultCode(): {}", curatorEvent.getResultCode());
                        // 获取操作类型 CREATE
                        logger.info("getType(): {}", curatorEvent.getType().toString());
                        // 获取节点路径
                        logger.info("getPath(): {}", curatorEvent.getPath());
                    }
                })
                .forPath("/node1", "testUpdate3".getBytes());
        logger.info("带有版本号更新{}/node1", namespace);
    }

    /**
     * 删除一个节点
     */
    @Test
    public void testDelete1() throws Exception {
        client.delete()
                .forPath("/node1");
        logger.info("删除节点{}/node1", namespace);
    }

    /**
     * 递归删除节点
     */
    @Test
    public void testDelete2() throws Exception {
        client.delete()
                .deletingChildrenIfNeeded()
                .forPath("/node3/subnode");
        logger.info("删除节点{}/node3/subnode", namespace);
    }

    /**
     * 带有回调方法删除一个节点
     *
     * @throws Exception
     */
    @Test
    public void testDelete3() throws Exception {
        client.delete()
                .deletingChildrenIfNeeded()
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                        logger.info("{}", curatorEvent.getType());  // DELETE
                        logger.info("{}", curatorEvent.getPath());  // /node1
                    }
                })
                .forPath("/node4/subnote");
        logger.info("删除结束");
    }

    /**
     * 查看一个节点
     */
    @Test
    public void testGet1() throws Exception {
        byte[] bytes = client.getData()
                .forPath("/node1");
        logger.info("/node1数据：{}", new String(bytes));
    }

    /**
     * 查看节点的值和状态
     */
    @Test
    public void testGet2() throws Exception {
        Stat stat = new Stat();
        byte[] bytes = client.getData()
                .storingStatIn(stat)
                .forPath("/node1");
        logger.info("/node1数据：{}, version: {}", new String(bytes), stat.getVersion());
    }

    /**
     * 带有回调方法的getdata
     *
     * @throws Exception
     */
    @Test
    public void testGet3() throws Exception {
        client.getData()
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                        logger.info(new String(curatorEvent.getData()));  // 4134134
                        logger.info(curatorEvent.getStat().toString());  // 21474836566,21474836566,1620042863998,1620042863998,0,0,0,0,7,0,21474836566
                        logger.info(curatorEvent.getType().toString());  // GET_DATA
                    }
                })
                .forPath("/node1");
    }

    /**
     * 查看一个节点的所有子节点
     *
     * @throws Exception
     */
    @Test
    public void testChildren1() throws Exception {
        List<String> children = client.getChildren()
                .forPath("/");
        logger.info(children.toString());
    }

    /**
     * 带有回调方法查看一个节点的所有子节点
     */
    @Test
    public void testChildren2() throws Exception {
        client.getChildren()
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        logger.info(event.getPath());
                        logger.info(event.getType().toString());
                        logger.info(event.getChildren().toString());
                    }
                })
                .forPath("/");
    }

    /**
     * 检查一个节点是否存在
     */
    @Test
    public void testExists1() throws Exception {
        Stat stat = client.checkExists()
                .forPath("/node");

        if (stat != null) {
            logger.info(stat.toString());
        } else {
            logger.info("节点不存在");
        }
    }

    @Test
    public void testExists2() throws Exception {
        client.checkExists()
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent event) throws Exception {
                        Stat stat = event.getStat();
                        if (stat != null) {
                            logger.info(stat.toString());
                        } else {
                            logger.info("节点不存在");
                        }
                    }
                })
                .forPath("/node");
    }

}
