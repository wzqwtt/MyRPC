package com.wzq.rpc.transport.socket;

import com.wzq.rpc.enumeration.RpcErrorMessageEnum;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.transport.RpcRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
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

    /**
     * 处理rpcRequest的类
     */
    private RpcRequestHandler rpcRequestHandler = new RpcRequestHandler();

    public SocketRpcServer() {
        // Runnable阻塞队列
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        // 线程工厂
        ThreadFactory threadFactory = Executors.defaultThreadFactory();

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
     * 服务端主动注册服务
     * TODO 1. 定义一个hashmap存放相关的service
     *      2. 修改为扫描注解注册
     *
     * @param service
     * @param port
     */
    public void register(Object service, int port) {
        // 判断注册的服务是否为空，如果为空则抛出异常
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_FOUND);
        }

        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("server starts...");
            Socket socket;

            while ((socket = server.accept()) != null) {
                logger.info("client connected");
                // 线程池执行任务
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            // 关闭线程池
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("occur IOException:", e);
        }
    }

}
