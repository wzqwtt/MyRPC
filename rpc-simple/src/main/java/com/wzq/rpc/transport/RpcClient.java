package com.wzq.rpc.transport;

import com.wzq.rpc.dto.RpcRequest;

/**
 * @author wzq
 * @create 2022-12-02 20:18
 */
public interface RpcClient {

    Object sendRpcRequest(RpcRequest rpcRequest);

}
