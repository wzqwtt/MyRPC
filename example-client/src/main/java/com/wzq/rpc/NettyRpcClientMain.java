package com.wzq.rpc;

import com.wzq.rpc.entity.RpcServiceProperties;
import com.wzq.rpc.proxy.RpcClientProxy;
import com.wzq.rpc.remoting.transport.netty.client.NettyClientTransport;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Netty客户端测试
 *
 * @author wzq
 * @create 2022-12-03 21:56
 */
public class NettyRpcClientMain {

    public static void main(String[] args) {
        // 获取Netty的RpcClient
        NettyClientTransport rpcClient = new NettyClientTransport();

        // 指定调用group test1的实现类
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().group("test1").version("version1").build();

        // 获取RpcClient动态代理类
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceProperties);

        // 获取HelloService接口的动态代理
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        // 获取HelloService接口的动态代理
        StudentService studentService = rpcClientProxy.getProxy(StudentService.class);

        // 进行服务调用
        for (int i = 0; i < 100; i++) {
            String hello = helloService.hello(new Hello("我想调用", "好啊，去远程调用吧！"));
            System.out.println(hello);
        }

//        for (int i = 0; i < 50; i++) {
//            List<Student> students = studentService.makeNStudent(i);
//            System.out.println(students);
//        }
    }

}
