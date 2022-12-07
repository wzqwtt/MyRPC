package com.wzq.rpc.remoting.transport.netty.client;

import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.serialize.Serializer;
import com.wzq.rpc.serialize.kryo.KryoSerializer;
import com.wzq.rpc.remoting.transport.netty.codec.kryo.NettySerializerDecoder;
import com.wzq.rpc.remoting.transport.netty.codec.kryo.NettySerializerEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * 用于初始化和关闭Bootstrap对象
 *
 * @author wzq
 * @create 2022-12-04 21:49
 */
@Slf4j
public final class NettyClient {

    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;

    // 初始化相关资源，比如:EventLoopGroup、BootStrap
    static {
        // 线程组
        eventLoopGroup = new NioEventLoopGroup();

        // 序列化器
        Serializer serializer = new KryoSerializer();

        // BootStrap配置
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                // 是否开启TCP底层心跳机制
                .option(ChannelOption.SO_KEEPALIVE, true)
                // 连接的超时时间，超过这个时间还是建立不上连接的话代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // TCP默认开启了Nagle算法，该算法的作用是尽可能发送大数据块，减少网络传输。
                // TCP_NODELAY参数的作用就是控制是否启用Nagle算法
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 添加自定义的序列化编解码器
                        // 解码器(入站): byteBuf -> RpcResponse
                        ch.pipeline().addLast(new NettySerializerDecoder(serializer, RpcResponse.class));
                        // 编码器(出站): RpcRequest -> byteBuf
                        ch.pipeline().addLast(new NettySerializerEncoder(serializer, RpcRequest.class));
                        // 自定义客户端入站Handler
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();

        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端连接成功!");
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });

        return completableFuture.get();
    }

    /**
     * 优雅的关闭eventLoopGroup线程组
     */
    public static void close() {
        log.info("call close method");
        eventLoopGroup.shutdownGracefully();
    }

}
