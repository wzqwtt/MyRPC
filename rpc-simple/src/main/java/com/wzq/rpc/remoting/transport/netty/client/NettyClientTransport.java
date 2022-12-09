package com.wzq.rpc.remoting.transport.netty.client;

import com.wzq.rpc.factory.SingletonFactory;
import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.registry.ServiceDiscovery;
import com.wzq.rpc.registry.zk.ZkServiceDiscovery;
import com.wzq.rpc.remoting.transport.ClientTransport;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

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
    private final ServiceDiscovery serviceDiscovery;

    private final UnprocessedRequests unprocessedRequests;

    private final ChannelProvider channelProvider;

    public NettyClientTransport() {
        this.serviceDiscovery = new ZkServiceDiscovery();
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 请求消息体
     * @return 服务端返回的数据
     */
    @Override
    public CompletableFuture<RpcResponse<Object>> sendRpcRequest(RpcRequest rpcRequest) {
        // 构建返回值
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();

        // 发现服务，找到所请求服务的地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        // 获取Channel
        Channel channel = channelProvider.get(inetSocketAddress);

        if (channel != null && channel.isActive()) {
            // 放入未处理的请求
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);

            // 发送消息
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info(String.format("client send message: %s", rpcRequest));
                } else {
                    future.channel().close();
                    // 如果任务没有执行成功，在resultFuture中放入异常
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }


}
