package com.wzq.rpc.remoting.transport.socket;

import com.wzq.rpc.config.CustomShutdownHook;
import com.wzq.rpc.entity.RpcServiceProperties;
import com.wzq.rpc.factory.SingletonFactory;
import com.wzq.rpc.provider.ServiceProvider;
import com.wzq.rpc.provider.ServiceProviderImpl;
import com.wzq.rpc.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

import static com.wzq.rpc.remoting.transport.netty.server.NettyServer.PORT;

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

    private final ServiceProvider serviceProvider;

    public SocketRpcServer(String host, int port) {
        // 使用抽象出去的线程池工厂类创建线程池
        threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent(THREAD_NAME_PREFIX);

        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    /**
     * 发布服务
     *
     * @param service Service
     */
    public void registerService(Object service) {
        serviceProvider.publishService(service);
    }

    /**
     * 发布服务
     *
     * @param service              Service
     * @param rpcServiceProperties Service相关的属性
     */
    public void registerService(Object service, RpcServiceProperties rpcServiceProperties) {
        serviceProvider.publishService(service, rpcServiceProperties);
    }

    /**
     * 启动服务端
     */
    public void start() {
        // 启动服务端
        try (ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, PORT));
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
