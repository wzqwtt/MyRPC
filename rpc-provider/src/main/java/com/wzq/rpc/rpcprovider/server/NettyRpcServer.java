package com.wzq.rpc.rpcprovider.server;

import com.wzq.rpc.rpcprovider.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RPC的服务端
 *
 * @author wzq
 * @create 2022-11-28 16:20
 */
@Slf4j
@Component
public class NettyRpcServer implements DisposableBean {

    EventLoopGroup bossGroup = null;
    EventLoopGroup workerGroup = null;

    /**
     * 共用一个编解码器
     */
    StringDecoder STRING_DECODER = new StringDecoder();
    StringEncoder STRING_ENCODER = new StringEncoder();

    // 自动注入NettyServerHandler
    @Autowired
    NettyServerHandler nettyServerHandler;

    public void start(String host, int port) {
        try {
            // 线程组
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            // 配置引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 设置启动参数
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            // 添加String的编解码器
                            ch.pipeline().addLast(STRING_DECODER);
                            ch.pipeline().addLast(STRING_ENCODER);
                            // 添加自定义Handler
                            ch.pipeline().addLast(nettyServerHandler);
                        }
                    });

            // 绑定IP以及端口号
            ChannelFuture channelFuture = serverBootstrap.bind(host, port).sync();
            log.info("==========RPC服务端启动完毕==========");
            // 监听通道的关闭状态
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 关闭资源
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        }

    }

    @Override
    public void destroy() throws Exception {
        // 关闭资源
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
