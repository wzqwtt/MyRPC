package com.wzq.rpc.rpcprovider;

import com.wzq.rpc.rpcprovider.server.NettyRpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RpcProviderApplication implements CommandLineRunner {

    private final static String DEFAULT_HOST = "localhost";
    private final static int DEFAULT_PORT = 8899;

    @Autowired
    NettyRpcServer nettyRpcServer;

    public static void main(String[] args) {
        SpringApplication.run(RpcProviderApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        new Thread(() -> {
            nettyRpcServer.start(DEFAULT_HOST, DEFAULT_PORT);
        }, "netty").start();
    }
}
