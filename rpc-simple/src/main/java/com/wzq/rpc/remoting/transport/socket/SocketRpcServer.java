package com.wzq.rpc.remoting.transport.socket;

import com.wzq.rpc.config.CustomShutdownHook;
import com.wzq.rpc.factory.SingletonFactory;
import com.wzq.rpc.provider.ServiceProvider;
import com.wzq.rpc.provider.ServiceProviderImpl;
import com.wzq.rpc.registry.ServiceRegistry;
import com.wzq.rpc.registry.zk.ZkServiceRegistry;
import com.wzq.rpc.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
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

    public SocketRpcServer(String host, int port) {
        // 使用抽象出去的线程池工厂类创建线程池
        threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent(THREAD_NAME_PREFIX);

        this.host = host;
        this.port = port;

        SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    /**
     * 启动服务端
     */
    public void start() {
        // 启动服务端
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(host, port));
            log.info("server starts...");
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;

            // 阻塞等待客户端连接
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
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
