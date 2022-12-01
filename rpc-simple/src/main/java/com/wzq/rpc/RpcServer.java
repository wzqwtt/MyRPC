package com.wzq.rpc;

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
        int corePoolSize = 10;
        int maximumPoolSize = 100;
        long keepAliveTime = 1;
        // TODO 线程池
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        this.threadPool = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.MINUTES,
                workQueue,
                threadFactory
        );
    }


    /**
     * 服务端主动注册服务
     * TODO 修改为注解然后扫描
     *
     * @param service
     * @param port
     */
    public void register(Object service, int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("server starts...");
            Socket socket;

            while ((socket = server.accept()) != null) {
                logger.info("client connected");
                threadPool.execute(new WorkerThread(socket, service));
            }
        } catch (IOException e) {
            logger.error("occur IOException:", e);
        }
    }

}
