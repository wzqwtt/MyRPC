package com.wzq.rpc;

import com.wzq.rpc.remoting.transport.netty.server.NettyServer;

/**
 * Netty服务端测试
 *
 * @author wzq
 * @create 2022-12-03 21:54
 */
public class NettyRpcServerMain1 {

    public static void main(String[] args) {
        // 手动注册服务到注册中心
        HelloServiceImpl helloService = new HelloServiceImpl();
        StudentServiceImpl studentService = new StudentServiceImpl();

        NettyServer nettyServer = new NettyServer("127.0.0.1", 9998);

        // 暴露服务
        nettyServer.publishService(helloService, HelloService.class);
        nettyServer.publishService(studentService, StudentService.class);

        nettyServer.start();
    }

}
