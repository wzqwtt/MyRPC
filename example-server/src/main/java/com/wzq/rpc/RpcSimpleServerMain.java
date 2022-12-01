package com.wzq.rpc;

/**
 * @author wzq
 * @create 2022-12-01 22:58
 */
public class RpcSimpleServerMain {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new RpcServer();
        rpcServer.register(helloService, 9999);
    }

}
