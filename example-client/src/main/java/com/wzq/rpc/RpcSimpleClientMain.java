package com.wzq.rpc;

import com.wzq.rpc.registry.DefaultServiceRegistry;
import com.wzq.rpc.transport.RpcClientProxy;
import com.wzq.rpc.transport.socket.SocketRpcClient;

/**
 * @author wzq
 * @create 2022-12-01 23:00
 */
public class RpcSimpleClientMain {

    public static void main(String[] args) {
        SocketRpcClient socketRpcClient = new SocketRpcClient("127.0.0.1", 9999);
        RpcClientProxy clientProxy = new RpcClientProxy(socketRpcClient);

        HelloService helloService = clientProxy.getProxy(HelloService.class);

        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }

}
