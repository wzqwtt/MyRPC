package com.wzq.rpc.transport.socket;

import com.wzq.rpc.utils.concurrent.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Socket RPC Server 由Socket执行的RPC服务端
 *
 * @author wzq
 * @create 2022-12-01 22:17
 */
public class SocketRpcServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketRpcServer.class);

    /**
     * 线程池
     */
    private final ExecutorService threadPool;

    /**
     * 自定义线程池名称
     */
    private static final String THREAD_NAME_PREFIX = "socket-server-rpc-pool";

    public SocketRpcServer() {
        // 使用抽象出去的线程池工厂类创建线程池
        threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }

    /**
     * 启动服务端
     *
     * @param port 服务端监听的端口号
     */
    public void start(int port) {
        // 启动服务端
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("server starts...");
            Socket socket;

            // 阻塞等待客户端连接
            while ((socket = server.accept()) != null) {
                logger.info("client connected");
                // 当有客户端连接，使用线程池执行任务
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
        } catch (IOException e) {
            logger.error("occur IOException:", e);
        } finally {
            // 关闭线程池
            threadPool.shutdown();
        }
    }

}
