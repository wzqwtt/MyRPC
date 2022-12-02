package com.wzq.rpc;

import com.wzq.rpc.enumeration.RpcErrorMessageEnum;
import com.wzq.rpc.exception.RpcException;
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
public class RpcServer {

    private ExecutorService threadPool;
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    public RpcServer() {
        // 线程池参数
        // 核心线程池大小
        int corePoolSize = 10;
        // 最大线程池大小
        int maximumPoolSize = 100;
        // 线程池中超过corePoolSize数目的空闲线程最大存活时间
        long keepAliveTime = 1;
        // 阻塞队列
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
        // 线程工厂
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        this.threadPool = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
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
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_NULL);
        }

        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("server starts...");
            Socket socket;

            while ((socket = server.accept()) != null) {
                logger.info("client connected");
                threadPool.execute(new ClientMessageHandlerThread(socket, service));
            }
        } catch (IOException e) {
            logger.error("occur IOException:", e);
        }
    }

}
