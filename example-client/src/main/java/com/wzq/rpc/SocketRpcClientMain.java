package com.wzq.rpc;

import com.wzq.rpc.transport.RpcClientProxy;
import com.wzq.rpc.transport.socket.SocketClientTransport;

/**
 * @author wzq
 * @create 2022-12-01 23:00
 */
public class SocketRpcClientMain {

    public static void main(String[] args) {
        // 获取一个Rpc客户端
        SocketClientTransport socketRpcClient = new SocketClientTransport("127.0.0.1", 9999);
        // 新建一个动态代理对象
        RpcClientProxy clientProxy = new RpcClientProxy(socketRpcClient);
        // 获取HelloService接口的动态代理实例
        HelloService helloService = clientProxy.getProxy(HelloService.class);

        // 调用方法
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }

}
