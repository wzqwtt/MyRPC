package com.wzq.rpc;

import com.wzq.rpc.registry.DefaultServiceRegistry;
import com.wzq.rpc.registry.ServiceRegistry;
import com.wzq.rpc.transport.netty.server.NettyServer;

/**
 * Netty服务端测试
 *
 * @author wzq
 * @create 2022-12-03 21:54
 */
public class NettyRpcServerMain {

    public static void main(String[] args) {
        // 手动注册服务到注册中心
        HelloServiceImpl helloService = new HelloServiceImpl();
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        serviceRegistry.register(helloService);

        // 启动NettyRpcServer
        NettyServer nettyRpcServer = new NettyServer(9999);
        nettyRpcServer.start();
    }

}
