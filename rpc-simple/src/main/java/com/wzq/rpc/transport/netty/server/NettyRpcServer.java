package com.wzq.rpc.transport.netty.server;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.serialize.Serializer;
import com.wzq.rpc.serialize.kryo.KryoSerializer;
import com.wzq.rpc.transport.netty.codec.NettySerializerDecoder;
import com.wzq.rpc.transport.netty.codec.NettySerializerEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty RPC服务端。接收客户端消息，并且根据客户端的消息调用相应的方法，然后返回结果给客户端。
 *
 * @author wzq
 * @create 2022-12-02 20:22
 */
public class NettyRpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    /**
     * 端口号
     */
    private final int port;

    /**
     * 序列化器
     */
    private final Serializer serializer;

    public NettyRpcServer(int port) {
        this.port = port;
        // TODO 添加更多的序列化器，由配置文件决定使用哪种序列化方式
        this.serializer = new KryoSerializer();
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // TODO 粘包半包问题
                            /* 自定义的序列化编解码器 */
                            // 解码器(入站): ByteBuf -> RpcRequest
                            ch.pipeline().addLast(new NettySerializerDecoder(serializer, RpcRequest.class));
                            // 编码器(出站): RpcResponse -> ByteBuf
                            ch.pipeline().addLast(new NettySerializerEncoder(serializer, RpcResponse.class));
                            // 自定义处理RpcRequest的处理器
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    })
                    // 设置TCP缓冲区
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口，同步等待绑定成功
            ChannelFuture channelFuture = b.bind(port).sync();

            // 等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("occur exception when start server: ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
