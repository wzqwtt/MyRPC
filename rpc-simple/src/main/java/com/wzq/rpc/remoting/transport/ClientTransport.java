package com.wzq.rpc.remoting.transport;

import com.wzq.rpc.remoting.dto.RpcRequest;

/**
 * 客户端接口，负责传输{@link RpcRequest}，并返回远程过程调用的结果
 *
 * @author wzq
 * @create 2022-12-02 20:18
 */
public interface ClientTransport {

    /**
     * 负责发送RpcRequest，并返回远程过程调用的结果
     *
     * @param rpcRequest 封装的RpcRequest消息
     * @return 返回远程过程调用的结果
     */
    Object sendRpcRequest(RpcRequest rpcRequest);

}
