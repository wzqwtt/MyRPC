package com.wzq.rpc;

import com.wzq.rpc.transport.socket.SocketRpcServer;

/**
 * @author wzq
 * @create 2022-12-01 22:58
 */
public class SocketRpcServerMain {

    public static void main(String[] args) {
        // new一个HelloServiceImpl实例
        HelloService helloService = new HelloServiceImpl();
        StudentServiceImpl studentService = new StudentServiceImpl();

        SocketRpcServer socketRpcServer = new SocketRpcServer("127.0.0.1", 7);

        socketRpcServer.publishService(helloService, HelloService.class);
        socketRpcServer.publishService(studentService, StudentService.class);

        socketRpcServer.start();
    }

}
