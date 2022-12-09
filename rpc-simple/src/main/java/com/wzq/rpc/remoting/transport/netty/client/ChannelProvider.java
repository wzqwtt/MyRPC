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
public class ChannelProvider {

    /**
     * eg: key:127.0.0.1:9999, value:channel
     */
    private final Map<String, Channel> channelMap;

    private final NettyClient nettyClient;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
        nettyClient = SingletonFactory.getInstance(NettyClient.class);
    }

    /**
     * 获取与对应服务端的Channel
     *
     * @param inetSocketAddress
     * @return
     */
    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();

        // 判断与该服务端的连接是否存在，如果存在就可以直接连接
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }

        // 否则，重新连接，获取Channel
        Channel channel = nettyClient.doConnect(inetSocketAddress);
        channelMap.put(key, channel);
        return channel;
    }

    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("Channel map size :[{}]", channelMap.size());
    }

}
