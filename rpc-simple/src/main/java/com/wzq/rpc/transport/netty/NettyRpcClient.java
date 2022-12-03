package com.wzq.rpc.transport.netty;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.serialize.Serializer;
import com.wzq.rpc.serialize.kryo.KryoSerializer;
import com.wzq.rpc.transport.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wzq
 * @create 2022-12-02 22:08
 */
public class NettyRpcClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);

    private String host;
    private int port;

    public NettyRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private static final Bootstrap b;

    // 初始化相关资源，比如:EventLoopGroup、BootStrap
    static {
        // 线程组
        EventLoopGroup group = new NioEventLoopGroup();

        // 序列化器
        Serializer serializer = new KryoSerializer();

        // BootStrap配置
        b = new Bootstrap();
        b.group(group)
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
     * 发送消息到服务端
     *
     * @param rpcRequest 请求消息体
     * @return 服务端返回的数据
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {

        try {
            ChannelFuture channelFuture = b.connect(host, port).sync();
            logger.info("client connect {}", host + ":" + port);

            Channel channel = channelFuture.channel();

            if (channel != null) {
                // 发送消息
                channel.writeAndFlush(rpcRequest).addListener(future -> {
                    if (future.isSuccess()) {
                        logger.info(String.format("client send message: %s", rpcRequest.toString()));
                    } else {
                        logger.error("Send failed:", future.cause());
                    }
                });

                channel.closeFuture().sync();

                // AttributeKey在Channel上共享数据，是线程安全的
                // 在AttributeKey中获取RpcResponse
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                RpcResponse rpcResponse = channel.attr(key).get();

                // 返回服务端响应的数据
                return rpcResponse.getData();
            }
        } catch (InterruptedException e) {
            logger.error("occur exception when connect server: ", e);
        }
        return null;
    }


}
