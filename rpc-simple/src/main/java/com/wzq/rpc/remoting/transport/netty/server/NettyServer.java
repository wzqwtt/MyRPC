package com.wzq.rpc.remoting.transport.netty.server;

import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.provider.ServiceProvider;
import com.wzq.rpc.provider.ServiceProviderImpl;
import com.wzq.rpc.registry.ServiceRegistry;
import com.wzq.rpc.registry.ZkServiceRegistry;
import com.wzq.rpc.serialize.Serializer;
import com.wzq.rpc.serialize.kryo.KryoSerializer;
import com.wzq.rpc.remoting.transport.netty.codec.kryo.NettySerializerDecoder;
import com.wzq.rpc.remoting.transport.netty.codec.kryo.NettySerializerEncoder;
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
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Netty RPC服务端。接收客户端消息，并且根据客户端的消息调用相应的方法，然后返回结果给客户端。
 *
 * @author wzq
 * @create 2022-12-02 20:22
 */
@Slf4j
public class NettyServer {
    
    /**
     * 主机和端口号
     */
    private final String host;
    private final int port;

    /**
     * 注册中心
     */
    private final ServiceRegistry serviceRegistry;

    /**
     * 服务的Provider
     */
    private final ServiceProvider serviceProvider;

    /**
     * 序列化器
     */
    private final Serializer serializer;

    public NettyServer(String host, int port) {
        this.host = host;
        this.port = port;
        // TODO(serializer) 添加更多的序列化器，由配置文件决定使用哪种序列化方式
        this.serializer = new KryoSerializer();

        // 注册中心
        serviceRegistry = new ZkServiceRegistry();
        // Provider
        serviceProvider = new ServiceProviderImpl();
    }

    /**
     * 暴露服务
     *
     * @param service      服务
     * @param serviceClass 服务的类型
     * @param <T>          服务的类型
     */
    public <T> void publishService(T service, Class<T> serviceClass) {
        // 注册到注册中心
        serviceRegistry.registerService(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        // 搞到Provider里面去
        serviceProvider.addServiceProvider(service, serviceClass);
//        start();
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
                            // TODO(sticky) 粘包半包问题
                            /* 自定义的序列化编解码器 */
                            // 解码器(入站): ByteBuf -> RpcRequest
                            ch.pipeline().addLast(new NettySerializerDecoder(serializer, RpcRequest.class));
                            // 编码器(出站): RpcResponse -> ByteBuf
                            ch.pipeline().addLast(new NettySerializerEncoder(serializer, RpcResponse.class));
                            // 自定义处理RpcRequest的处理器
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    })
                    // TCP默认开启了Nagle算法，该算法的作用是尽可能的发送大数据块，减少网络传输。
                    // TCP_NODELAY参数的作用就是控制是否启用Nagle算法
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 表示系统用于临时存放已完成三次握手的请求队列的最大长度，如果连接建立频繁，服务器吃创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 是否开启TCP底层心跳机制
                    .option(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口，同步等待绑定成功
            ChannelFuture channelFuture = b.bind(host, port).sync();

            // 等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server: ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
