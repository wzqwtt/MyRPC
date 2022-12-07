package com.wzq.rpc.remoting.transport.netty.client;

import com.wzq.rpc.factory.SingletonFactory;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于获取Channel对象，带有重试机制
 *
 * @author wzq
 * @create 2022-12-04 21:57
 */
@Slf4j
public final class ChannelProvider {

    /**
     * eg: key:127.0.0.1:9999, value:channel
     */
    private static Map<String, Channel> channels = new ConcurrentHashMap<>();

    private static NettyClient nettyClient;

    static {
        nettyClient = SingletonFactory.getInstance(NettyClient.class);
    }

    private ChannelProvider() {
    }

    /**
     * 获取与对应服务端的Channel
     *
     * @param inetSocketAddress
     * @return
     */
    public static Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();

        // 如果已经有可用连接就直接获取
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }

        // 否则，重新连接，获取Channel
        Channel channel = nettyClient.doConnect(inetSocketAddress);
        channels.put(key, channel);
        return channel;
    }

}
