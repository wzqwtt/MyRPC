package com.wzq.rpc.transport.netty.client;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.serialize.Serializer;
import com.wzq.rpc.serialize.kryo.KryoSerializer;
import com.wzq.rpc.transport.netty.codec.NettySerializerDecoder;
import com.wzq.rpc.transport.netty.codec.NettySerializerEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于初始化和关闭Bootstrap对象
 *
 * @author wzq
 * @create 2022-12-04 21:49
 */
public final class NettyClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    /**
     * 私有化构造方法
     */
    private NettyClient() {
    }

    private static final Bootstrap b;
    private static final EventLoopGroup eventLoopGroup;

    // 初始化相关资源，比如:EventLoopGroup、BootStrap
    static {
        // 线程组
        eventLoopGroup = new NioEventLoopGroup();

        // 序列化器
        Serializer serializer = new KryoSerializer();

        // BootStrap配置
        b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
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

    /**
     * 获取BootStrap
     *
     * @return 返回BootStrap
     */
    public static Bootstrap initializeBootstrap() {
        return b;
    }

    /**
     * 优雅的关闭eventLoopGroup线程组
     */
    public static void close() {
        logger.info("call close method");
        eventLoopGroup.shutdownGracefully();
    }

}
