package com.wzq.rpc;

/**
 * @author wzq
 * @create 2022-12-01 23:00
 */
public class RpcSimpleClientMain {

    public static void main(String[] args) {
        RpcClientProxy rpcClientProxy = new RpcClientProxy("127.0.0.1", 9999);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);

        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }

}
