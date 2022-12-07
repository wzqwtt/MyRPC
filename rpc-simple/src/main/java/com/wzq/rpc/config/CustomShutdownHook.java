package com.wzq.rpc.config;

import com.wzq.rpc.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import com.wzq.rpc.utils.zk.CuratorUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * 当服务端（Provider）关闭的时候做一些事情，比如：清楚所有注册的服务
 *
 * @author wzq
 * @create 2022-12-06 16:19
 */
@Slf4j
public class CustomShutdownHook {

    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        // 在JVM销毁前执行一个线程
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CuratorUtils.clearRegistry();
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}
