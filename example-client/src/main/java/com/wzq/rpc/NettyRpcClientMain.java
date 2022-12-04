package com.wzq.rpc;

import com.wzq.rpc.transport.RpcClientProxy;
import com.wzq.rpc.transport.netty.client.NettyRpcClient;

/**
 * Netty客户端测试
 *
 * @author wzq
 * @create 2022-12-03 21:56
 */
public class NettyRpcClientMain {

    public static void main(String[] args) {
        // 获取Netty的RpcClient
        NettyRpcClient nettyRpcClient = new NettyRpcClient("localhost", 9999);
        // 获取RpcClient动态代理类
        RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyRpcClient);
        // 获取HelloService接口的动态代理
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("我想调用", "好啊，去远程调用吧！"));
        System.out.println(hello);
    }

}
