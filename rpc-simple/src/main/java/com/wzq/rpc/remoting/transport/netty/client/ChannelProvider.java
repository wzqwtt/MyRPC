package com.wzq.rpc.remoting.transport.netty.client;

import com.wzq.rpc.enumeration.RpcErrorMessageEnum;
import com.wzq.rpc.exception.RpcException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 用于获取Channel对象
 *
 * @author wzq
 * @create 2022-12-04 21:57
 */
@Slf4j
public class ChannelProvider {

    /**
     * 客户端Bootstrap对象，通过{@link NettyClient}获取
     */
    private static Bootstrap bootstrap = NettyClient.initializeBootstrap();

    /**
     * Channel对象，客户端与服务端连接的桥梁
     */
    private static Channel channel = null;

    /**
     * 客户端最多重连次数
     */
    private static final int MAX_RETRY_COUNT = 5;

    /**
     * 获取
     *
     * @param inetSocketAddress
     * @return
     */
    public static Channel get(InetSocketAddress inetSocketAddress) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        // 在此处等待客户端连接，如果连接上，这里就不阻塞了
        connect(bootstrap, inetSocketAddress, countDownLatch);
        countDownLatch.await();

        return channel;
    }

    public static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, CountDownLatch countDownLatch) {
        connect(bootstrap, inetSocketAddress, MAX_RETRY_COUNT, countDownLatch);
    }

    /**
     * 带有重试机制的客户端连接方法
     *
     * @param bootstrap         Bootstrap
     * @param inetSocketAddress InetSocketAddress
     * @param retry             重试次数
     * @param countDownLatch    锁
     */
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress,
                                int retry, CountDownLatch countDownLatch) {
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端连接成功!");
                channel = future.channel();
                countDownLatch.countDown();
                return;
            }

            if (retry == 0) {
                log.error("客户端连接失败: 重试次数已用完，放弃连接!");
                countDownLatch.countDown();
                NettyClient.close();
                throw new RpcException(RpcErrorMessageEnum.CLIENT_CONNECT_SERVER_FAILURE);
            }

            // 第几次重连
            int order = (MAX_RETRY_COUNT - retry) + 1;
            // 本次重连的间隔
            int delay = 1 << order;
            log.error("{}: 连接失败，第 {} 次重连......", new Date(), order);
            bootstrap.config().group().schedule(
                    () -> connect(bootstrap, inetSocketAddress, retry - 1, countDownLatch),
                    delay,
                    TimeUnit.SECONDS
            );
        });
    }
}
