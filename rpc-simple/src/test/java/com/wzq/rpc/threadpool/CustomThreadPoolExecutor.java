package com.wzq.rpc.threadpool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定制非阻塞线程池
 *
 * @author wzq
 * @create 2022-12-02 14:22
 */
public class CustomThreadPoolExecutor {

    private ThreadPoolExecutor pool = null;

    /**
     * 线程池初始化方法
     * <ul>
     *     <li>corePoolSize 核心线程池大小----10</li>
     *     <li>maximumPoolSize 最大线程池大小----30</li>
     *     <li>keepAliveTime 线程池中超过corePoolSize数目的空闲线程最大存活时间----30+单位TimeUnit</li>
     *     <li>TimeUnit keepAliveTime时间单位----TimeUnit.MINUTES</li>
     *     <li>workQueue 阻塞队列----new ArrayBlockingQueue<Runnable>(10)====10容量的阻塞队列</li>
     *     <li>threadFactory 新建线程工厂----new CustomThreadFactory()====定制的线程工厂</li>
     *     <li>rejectedExecutionHandler 当提交任务数超过maxmumPoolSize+workQueue之和时,
     *          即当提交第41个任务时(前面线程都没有执行完,此测试方法中用sleep(100)),
     *          任务会交给RejectedExecutionHandler来处理
     *     </li>
     * </ul>
     */
    public void init() {
        pool = new ThreadPoolExecutor(
                10,
                30,
                30,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(10),
                new CustomThreadFactory(),
                new CustomRejectedExecutionHandler()
        );
    }

    public ExecutorService getCustomThreadPoolExecutor() {
        return this.pool;
    }

    public void destory() {
        if (pool != null) {
            pool.shutdownNow();
        }
    }

    private class CustomThreadFactory implements ThreadFactory {

        private AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            String threadName = CustomThreadPoolExecutor.class.getSimpleName() + count.addAndGet(1);
            System.out.println(threadName);
            t.setName(threadName);
            return t;
        }
    }

    private class CustomRejectedExecutionHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 记录异常
            // 报警处理等
            System.out.println("error.....");
        }
    }

    // 测试构造的线程池
    public static void main(String[] args) {
        CustomThreadPoolExecutor exec = new CustomThreadPoolExecutor();
        // 1.初始化
        exec.init();

        ExecutorService pool = exec.getCustomThreadPoolExecutor();
        for (int i = 1; i < 100; i++) {
            System.out.println("提交第" + i + "个任务!");
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("running=====");
                }
            });
        }


        // 2.销毁----此处不能销毁,因为任务没有提交执行完,如果销毁线程池,任务也就无法执行了
        // exec.destory();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}