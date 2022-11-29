package com.wzq.rpc.rpcconsumer.client;

import com.wzq.rpc.rpcconsumer.handler.NettyRpcClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

/**
 * Netty客户端，项目启动就启动Netty连接服务端，因此需要实现Spring的生命周期方法
 *
 * @author wzq
 * @create 2022-11-29 16:42
 */
@Slf4j
@Component
public class NettyRpcClient implements InitializingBean, DisposableBean {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8899;

    EventLoopGroup group = null;
    Channel channel = null;

    /**
     * StringDe/EnCoder编解码器
     */
    static StringEncoder STRING_ENCODER = new StringEncoder();
    static StringDecoder STRING_DECODER = new StringDecoder();

    @Autowired
    NettyRpcClientHandler nettyRpcClientHandler;

    /**
     * Netty线程池
     */
    EventLoopGroup defaulGroup = null;

    /**
     * 连接服务端
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            group = new NioEventLoopGroup();
            defaulGroup = new DefaultEventLoop();

            // 配置Netty的引导类
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            // String编解码器
                            ch.pipeline().addLast(STRING_DECODER);
                            ch.pipeline().addLast(STRING_ENCODER);

                            // 自定义处理类
                            ch.pipeline().addLast(nettyRpcClientHandler);
                        }
                    });

            // 连接到服务端
            channel = bootstrap.connect(DEFAULT_HOST, DEFAULT_PORT).sync().channel();
        } catch (InterruptedException e) {
            // 出现异常，关闭资源
            e.printStackTrace();
            if (channel != null) {
                channel.close();
            }
            if (group != null) {
                group.shutdownGracefully();
            }
            if (defaulGroup != null) {
                defaulGroup.shutdownGracefully();
            }
        }

    }

    /**
     * 关闭资源
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
        if (defaulGroup != null) {
            defaulGroup.shutdownGracefully();
        }
    }

    /**
     * 消息发送
     *
     * @param msg
     * @return
     */
    public Object send(String msg) throws ExecutionException, InterruptedException {
        // 往netty里面放入要发送的消息
        nettyRpcClientHandler.setReqMsg(msg);
        // 使用Netty默认线程池执行线程
        Future future = defaulGroup.submit(nettyRpcClientHandler);
        // 返回服务端发送过来的消息
        return future.get();
    }
}
