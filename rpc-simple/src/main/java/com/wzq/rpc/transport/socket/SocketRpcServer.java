package com.wzq.rpc.transport.socket;

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
     * 线程池参数
     */
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAXIMUM_POOL_SIZE = 100;
    private static final int KEPP_ALIVE_TIME = 1;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    /**
     * 线程池
     */
    private ExecutorService threadPool;

    public SocketRpcServer() {
        // Runnable阻塞队列
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        // 线程工厂
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        // 线程池
        this.threadPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEPP_ALIVE_TIME,
                // keepAliveTime时间单位
                TimeUnit.MINUTES,
                workQueue,
                threadFactory
        );
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
