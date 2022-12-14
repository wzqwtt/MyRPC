package com.wzq.rpc;

import com.wzq.rpc.annotation.RpcScan;
import com.wzq.rpc.remoting.transport.netty.server.NettyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Netty服务端测试
 *
 * @author wzq
 * @create 2022-12-03 21:54
 */
@RpcScan(basePackage = {"com.wzq.rpc.service"})
public class NettyRpcServerMain {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyRpcServerMain.class);
//        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        NettyServer nettyServer = new NettyServer();
        nettyServer.start();
    }

}
