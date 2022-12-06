package com.wzq.rpc.transport.socket;

import com.wzq.rpc.provider.ServiceProvider;
import com.wzq.rpc.provider.ServiceProviderImpl;
import com.wzq.rpc.registry.ServiceRegistry;
import com.wzq.rpc.registry.ZkServiceRegistry;
import com.wzq.rpc.utils.concurrent.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Socket RPC Server 由Socket执行的RPC服务端
 *
 * @author wzq
 * @create 2022-12-01 22:17
 */
@Slf4j
public class SocketRpcServer {
    
    /**
     * 线程池
     */
    private final ExecutorService threadPool;

    /**
     * 自定义线程池名称
     */
    private static final String THREAD_NAME_PREFIX = "socket-server-rpc-pool";

    /**
     * 主机和端口
     */
    private final String host;
    private final int port;

    /**
     * 注册中心
     */
    private final ServiceRegistry serviceRegistry;

    /**
     * Provider
     */
    private final ServiceProvider serviceProvider;

    public SocketRpcServer(String host, int port) {
        // 使用抽象出去的线程池工厂类创建线程池
        threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);

        this.host = host;
        this.port = port;
        serviceRegistry = new ZkServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
    }

    /**
     * 暴露服务
     *
     * @param service      服务的实现类
     * @param serviceClass 类型
     * @param <T>          服务的类型
     */
    public <T> void publishService(T service, Class<T> serviceClass) {
        // 将服务添加到Provider
        serviceProvider.addServiceProvider(service, serviceClass);
        // 注册服务到zookeeper
        serviceRegistry.registerService(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
//        start();
    }

    /**
     * 启动服务端
     */
    public void start() {
        // 启动服务端
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(host, port));
            log.info("server starts...");
            Socket socket;

            // 阻塞等待客户端连接
            while ((socket = server.accept()) != null) {
                log.info("client connected");
                // 当有客户端连接，使用线程池执行任务
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
        } catch (IOException e) {
            log.error("occur IOException:", e);
        } finally {
            // 关闭线程池
            threadPool.shutdown();
        }
    }

}
