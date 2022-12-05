package com.wzq.rpc;

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
        StudentServiceImpl studentService = new StudentServiceImpl();

        NettyServer nettyServer = new NettyServer("127.0.0.1", 9999);
        nettyServer.publishService(helloService, HelloService.class);
        // TODO 注册中心只能注册一个Service
        nettyServer.publishService(studentService, StudentService.class);

        nettyServer.start();
    }

}
