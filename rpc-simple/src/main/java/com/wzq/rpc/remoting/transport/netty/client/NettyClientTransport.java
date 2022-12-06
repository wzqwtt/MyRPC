package com.wzq.rpc.remoting.transport.netty.client;

import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.registry.ServiceDiscovery;
import com.wzq.rpc.registry.ZkServiceDiscovery;
import com.wzq.rpc.remoting.transport.ClientTransport;
import com.wzq.rpc.remoting.dto.RpcMessageChecker;
import io.netty.channel.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Netty Rpc客户端。发送消息到服务端，并接收服务端返回的方法执行结果。
 *
 * @author wzq
 * @create 2022-12-02 22:08
 */
@Slf4j
public class NettyClientTransport implements ClientTransport {
    
    /**
     * 服务发现
     */
    private ServiceDiscovery serviceDiscovery;

    public NettyClientTransport() {
        serviceDiscovery = new ZkServiceDiscovery();
    }

    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 请求消息体
     * @return 服务端返回的数据
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 使用原子类保存结果，最终返回
        AtomicReference<Object> result = new AtomicReference<>(null);

        try {
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            // 获取Channel
            Channel channel = ChannelProvider.get(inetSocketAddress);

            if (channel.isActive()) {
                // 发送消息
                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info(String.format("client send message: %s", rpcRequest.toString()));
                    } else {
                        future.channel().close();
                        log.error("Send failed:", future.cause());
                    }
                });

                channel.closeFuture().sync();

                // AttributeKey在Channel上共享数据，是线程安全的
                // 在AttributeKey中获取RpcResponse
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse rpcResponse = channel.attr(key).get();
                log.info("client get rpcResponse from channel:{}", rpcResponse);

                // 校验request和response
                RpcMessageChecker.check(rpcResponse, rpcRequest);

                // 返回服务端响应的数据
                result.set(rpcResponse.getData());
            } else {
                NettyClient.close();
                System.exit(0);
            }
        } catch (InterruptedException e) {
            log.error("occur exception when connect server: ", e);
        }

        return result.get();
    }


}
