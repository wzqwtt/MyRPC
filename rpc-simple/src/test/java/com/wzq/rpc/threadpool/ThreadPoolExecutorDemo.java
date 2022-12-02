package com.wzq.rpc.threadpool;

import java.util.concurrent.*;

/**
 * ThreadPoolExecutor机制
 * <p>
 * 一、概述
 *
 * <ul>
 *     <li>ThreadPoolExecutor作为java.util.concurrent包对外提供的基础实现，以内部线程池的形式对外提供管理任务执行，线程调度，线程池管理等等服务</li>
 *     <li>Executors方法提供的线程服务，都是通过参数设置来实现不同的线程池机制</li>
 *     <li>先来了解其线程池管理机制，有助于正确使用，避免错误使用导致严重故障。同时可以根据自己的需求实现自己的线程池</li>
 * </ul>
 * <p>
 * 二、核心构造方法
 *
 * <table>
 *     <tr> <th>参数名</th> <th>作用</th> </tr>
 *     <tr> <td>corePoolSize</td> <td>核心线程池大小</td> </tr>
 *     <tr> <td>maximumPoolSize</td> <td>最大线程池大小</td> </tr>
 *     <tr> <td>keepAliveTime</td> <td>线程池中超过corePoolSize数目的空闲最大存活时间；<br>
 *     可以allowCoreThreadTimeOut(true)使得核心线程有效时间 </td> </tr>
 *     <tr> <td>TimeUnit</td> <td>keepAliveTime时间单位</td> </tr>
 *     <tr> <td>workQueue</td> <td>阻塞任务队列</td> </tr>
 *     <tr> <td>threadFactory</td> <td>新建线程工厂</td> </tr>
 *     <tr> <td>RejectedExecutionHandler</td> <td>当提交任务数超过maximumPoolSize+workQueue之和时，<br>
 *     任务会交给RejectedExecutionHandler来处理</td> </tr>
 * </table>
 * <p>
 * 其中比较容易让人误解的是：corePoolSize,maximumPoolSize,workQueue之间的关系
 * <ul>
 *     <li>当线程池小于corePoolSize时，新提交任务将创建一个新线程执行任务，即使此时线程池中存在空闲线程</li>
 *     <li>当线程池达到corePoolSize时，新提交任务将被放入workQueue中，等待线程池中任务调度执行</li>
 *     <li>当workQueue已满，且maximumPoolSize > corePoolSize时，新提交任务会创建新线程执行任务</li>
 *     <li>当提交任务数超过maximumPoolSize时，新提交任务由RejectedExecutionHandler处理</li>
 *     <li>当线程池这种超过corePoolSize线程，空闲时间达到keepAliveTime时，关闭空闲线程</li>
 *     <li>当设置allowCoreThreadTimeOut(true)时，线程池中corPoolSize线程空闲时间达到KeepAliveTime也将关闭</li>
 * </ul>
 *
 * @author wzq
 * @create 2022-12-02 13:47
 */
public class ThreadPoolExecutorDemo {

    /* 下列演示的是：Executor提供的一些线程池配置方案 */

    /**
     * 构造一个固定线程数目的线程池，配置的corePoolSize与maximumPoolSize大小相同，同时使用了一个
     * 无界LinkedBlockingQueue存放阻塞任务，因此多余的任务将存在在阻塞队列，不会由RejectedExecutionHandler处理
     *
     * @param nThread 固定线程数目
     * @return 返回一个线程池
     */
    public static ExecutorService newFixedThreadPool(int nThread) {
        return new ThreadPoolExecutor(nThread, nThread,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    /**
     * 构造一个缓冲功能的线程池，配置corePoolSize=0, maximumPoolSize=Integer.MAX_VALUE,
     * keepAliveTime=60s，以及一个无容量的阻塞队列SynchronousQueue，因此任务提交之后，将会
     * 创建新的线程执行；线程空闲超过60s将会销毁
     *
     * @return 返回一个线程池
     */
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    /**
     * 构造一个只支持一个线程的线程池，配置corePoolSize=maximumPoolSize=1, 无界阻塞队列
     * LinkedBlockingQueue; 保证任务由一个线程串执行
     *
     * @return 返回一个线程池
     */
//    public static ExecutorService newSingleThreadExecutor() {
//        return new Executors.FinalizableDelegatedExecutorService
//                (new ThreadPoolExecutor(1, 1,
//                        0L, TimeUnit.MILLISECONDS,
//                        new LinkedBlockingQueue<Runnable>()));
//    }

}
