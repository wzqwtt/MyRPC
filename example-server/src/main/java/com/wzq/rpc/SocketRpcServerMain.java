package com.wzq.rpc;

import com.wzq.rpc.registry.DefaultServiceRegistry;
import com.wzq.rpc.registry.ServiceRegistry;
import com.wzq.rpc.transport.socket.SocketRpcServer;

/**
 * @author wzq
 * @create 2022-12-01 22:58
 */
public class SocketRpcServerMain {

    public static void main(String[] args) {
        // new一个HelloServiceImpl实例
        HelloService helloService = new HelloServiceImpl();
        // 注册中心
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        // 手动注册：将helloService注册到注册中心
        serviceRegistry.register(helloService);

        // 启动RpcServer服务器
        SocketRpcServer rpcServer = new SocketRpcServer();
        rpcServer.start(9999);
    }

}
