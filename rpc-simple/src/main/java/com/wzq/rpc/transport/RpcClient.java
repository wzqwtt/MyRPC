package com.wzq.rpc.transport;

import com.wzq.rpc.dto.RpcRequest;

/**
 * 客户端接口，负责发送{@link RpcRequest}，并返回远程过程调用的结果
 *
 * @author wzq
 * @create 2022-12-02 20:18
 */
public interface RpcClient {

    /**
     * 负责发送RpcRequest，并返回远程过程调用的结果
     *
     * @param rpcRequest 封装的RpcRequest消息
     * @return 返回远程过程调用的结果
     */
    Object sendRpcRequest(RpcRequest rpcRequest);

}
