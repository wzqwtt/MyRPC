package com.wzq.rpc.transport.netty.client;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.registry.ServiceRegistry;
import com.wzq.rpc.registry.ZkServiceRegistry;
import com.wzq.rpc.transport.ClientTransport;
import com.wzq.rpc.utils.checker.RpcMessageChecker;
import io.netty.channel.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Netty Rpc客户端。发送消息到服务端，并接收服务端返回的方法执行结果。
 *
 * @author wzq
 * @create 2022-12-02 22:08
 */
public class NettyClientTransport implements ClientTransport {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientTransport.class);

    private ServiceRegistry serviceRegistry;

    public NettyClientTransport() {
        serviceRegistry = new ZkServiceRegistry();
    }

    public NettyClientTransport(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
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
            InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(rpcRequest.getInterfaceName());
            // 获取Channel
            Channel channel = ChannelProvider.get(inetSocketAddress);

            if (channel.isActive()) {
                // 发送消息
                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        logger.info(String.format("client send message: %s", rpcRequest.toString()));
                    } else {
                        future.channel().close();
                        logger.error("Send failed:", future.cause());
                    }
                });

                channel.closeFuture().sync();

                // AttributeKey在Channel上共享数据，是线程安全的
                // 在AttributeKey中获取RpcResponse
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse rpcResponse = channel.attr(key).get();
                logger.info("client get rpcResponse from channel:{}", rpcResponse);

                // 校验request和response
                RpcMessageChecker.check(rpcResponse, rpcRequest);

                // 返回服务端响应的数据
                result.set(rpcResponse.getData());
            } else {
                System.exit(0);
            }
        } catch (InterruptedException e) {
            logger.error("occur exception when connect server: ", e);
        }

        return result.get();
    }


}
