package com.wzq.rpc;

import com.wzq.rpc.provider.ServiceProvider;
import com.wzq.rpc.provider.ServiceProviderImpl;
import com.wzq.rpc.remoting.transport.netty.server.NettyServer;
import com.wzq.rpc.service.HelloServiceImpl;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 使用API主动暴露服务
 *
 * @author wzq
 * @create 2022-12-09 19:08
 */
public class NettyRpcServerMain2 {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();

        // 获取Bean
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyRpcServerMain.class);
        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);

        // 开启NettyServer端
        nettyServer.start();

        // 主动暴露服务
        ServiceProvider serviceProvider = new ServiceProviderImpl();
        serviceProvider.publishService(helloService);
    }

}
