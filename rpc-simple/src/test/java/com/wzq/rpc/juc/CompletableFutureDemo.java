package com.wzq.rpc.juc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.*;

/**
 * {@link CompletableFuture}源码中有四个静态方法用来执行异步任务
 * <ul>
 *     <li><b>有返回值：</b></li>
 *     <li>{@code public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)}</li>
 *     <li>{@code public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)}</li>
 *     <li><b>无返回值：</b></li>
 *     <li>{@code public static CompletableFuture<Void> runAsync(Runnable runnable)}</li>
 *     <li>{@code public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)}</li>
 * </ul>
 * 如果没有传递{@code Executor}对象，将会使用{@code ForkJoinPool.commonPool()}作为它的线程池执行异步代码
 * <p>
 * 获取执行结果的几个方法：
 * <ul>
 *     <li>{@code V get();}，会阻塞当前线程</li>
 *     <li>{@code V get(long timeout, Timeout unit);}，可以设置等待的时间</li>
 *     <li>{@code T getNow(T defaultValue);}，当有返回结果就返回，如果抛出异常则会返回指定的默认值</li>
 *     <li>{@code T join();}</li>
 * </ul>
 *
 *
 * @author wzq
 * @create 2022-12-07 15:31
 */
@Slf4j
public class CompletableFutureDemo {

    /**
     * Callable形式，返回Future，get方法获取结果时候是阻塞的
     */
    @Test
    public void futureTest() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<String> stringFuture = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                TimeUnit.SECONDS.sleep(2);
                return "async thread";
            }
        });

        TimeUnit.SECONDS.sleep(1);
        log.info("main thread");
        // 1秒后打印future结果，当前main线程阻塞！
        log.info(stringFuture.get());
        log.info("end");
    }

    /**

     */
    @Test
    public void test() {

    }

}
