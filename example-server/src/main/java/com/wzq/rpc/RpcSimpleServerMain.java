package com.wzq.rpc;

import com.wzq.rpc.registry.DefaultServiceRegistry;
import com.wzq.rpc.registry.ServiceRegistry;
import com.wzq.rpc.remoting.socket.RpcServer;

/**
 * @author wzq
 * @create 2022-12-01 22:58
 */
public class RpcSimpleServerMain {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        // 注册中心
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        // 手动注册
        serviceRegistry.register(helloService);
        RpcServer rpcServer = new RpcServer(serviceRegistry);
        rpcServer.register(helloService, 9999);
    }

}
