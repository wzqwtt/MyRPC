package com.wzq.rpc.remoting.transport.netty.server;

import com.wzq.rpc.config.CustomShutdownHook;
import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.remoting.dto.RpcResponse;
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
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * Netty RPC服务端。接收客户端消息，并且根据客户端的消息调用相应的方法，然后返回结果给客户端。
 *
 * @author wzq
 * @create 2022-12-02 20:22
 */
@Slf4j
@Component
public class NettyServer implements InitializingBean {

    /**
     * 端口
     */
    public static final int PORT = 9998;

    /**
     * 序列化器
     */
    private final Serializer serializer = new KryoSerializer();

    @SneakyThrows
    public void start() {
        String host = InetAddress.getLocalHost().getHostAddress();

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
                            // 30秒内没有收到客户端请求的话就关闭连接
                            ch.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
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
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 是否开启TCP底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口，同步等待绑定成功
            ChannelFuture channelFuture = b.bind(host, PORT).sync();

            // 等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server: ", e);
        } finally {
            log.error("shutdown bossGroup and workGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 在初始化NettyServer类中，设置关闭钩子
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 设置关闭钩子，待JVM退出时执行一个线程，关闭所有资源
        CustomShutdownHook.getCustomShutdownHook().clearAll();
    }

}
