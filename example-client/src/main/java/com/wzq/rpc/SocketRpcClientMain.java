package com.wzq.rpc;

import com.wzq.rpc.proxy.RpcClientProxy;
import com.wzq.rpc.transport.ClientTransport;
import com.wzq.rpc.transport.socket.SocketRpcClient;

import java.util.List;

/**
 * @author wzq
 * @create 2022-12-01 23:00
 */
public class SocketRpcClientMain {

    public static void main(String[] args) {
        // 获取一个Rpc客户端
        ClientTransport clientTransport = new SocketRpcClient();
        // 新建一个动态代理对象，传递一个数据传输对象，后续的sendRpcRequest由这个数据传输对象负责发起请求、接收响应
        RpcClientProxy clientProxy = new RpcClientProxy(clientTransport);

        // 获取HelloService接口的动态代理实例
        HelloService helloService = clientProxy.getProxy(HelloService.class);
        StudentService studentService = clientProxy.getProxy(StudentService.class);

        // 调用方法
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);

        List<Student> students = studentService.makeNStudent(5);
        System.out.println(students);
    }

}
