package com.wzq.rpc.remoting.transport.netty.client;

import com.wzq.rpc.remoting.dto.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 未处理的请求
 *
 * @author wzq
 * @create 2022-12-06 16:56
 */
public class UnprocessedRequests {

    /**
     * 记录消息和CompletableFuture的集合
     */
    private static ConcurrentHashMap<String, CompletableFuture<RpcResponse>> unprocessedResponseFutures = new ConcurrentHashMap<>();

    /**
     * 放入未处理的消息
     *
     * @param requestId 键
     * @param future    值
     */
    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        unprocessedResponseFutures.put(requestId, future);
    }

    /**
     * 移除一个未处理的消息
     *
     * @param requestId 键
     */
    public void remove(String requestId) {
        unprocessedResponseFutures.remove(requestId);
    }

    /**
     * 设置消息已经处理完成，并且从map中remove掉
     *
     * @param rpcResponse 服务端响应的消息
     */
    public void complete(RpcResponse rpcResponse) {
        CompletableFuture<RpcResponse> future = unprocessedResponseFutures.remove(rpcResponse.getRequestId());

        if (future != null) {
            // 将该future设置结果
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
