package com.wzq.rpc.remoting.transport.netty.server;

import com.wzq.rpc.annotation.RpcService;
import com.wzq.rpc.config.CustomShutdownHook;
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
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Netty RPC服务端。接收客户端消息，并且根据客户端的消息调用相应的方法，然后返回结果给客户端。
 *
 * @author wzq
 * @create 2022-12-02 20:22
 */
@Slf4j
@Component
@PropertySource("classpath:rpc.properties")
public class NettyServer implements InitializingBean, ApplicationContextAware {

    /**
     * 主机和端口号
     */
    @Value("${rpc.server.host}")
    private String host;

    @Value("${rpc.server.port}")
    private int port;

    /**
     * 注册中心
     */
    private final ServiceRegistry serviceRegistry = new ZkServiceRegistry();

    /**
     * 服务的Provider
     */
    private final ServiceProvider serviceProvider = new ServiceProviderImpl();

    /**
     * 序列化器
     */
    private final Serializer serializer = new KryoSerializer();

    /**
     * 暴露服务
     *
     * @param service      服务
     * @param serviceClass 服务的类型
     * @param <T>          服务的类型
     */
    public void publishService(Object service, Class<?> serviceClass) {
        // 注册到注册中心
        // getCanonicalName: com.wzq.rpc.HelloService
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
            ChannelFuture channelFuture = b.bind(host, port).sync();

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

    /**
     * 获取所有被RpcService注解的类，将这些类推送到zookeeper注册中心
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取所有标注@RpcService注解的类
        Map<String, Object> registeredBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        // 循环遍历所有元素，进行注册
        for (Map.Entry<String, Object> entry : registeredBeanMap.entrySet()) {
            Object obj = entry.getValue();
            publishService(obj, obj.getClass().getInterfaces()[0]);
        }
    }
}
