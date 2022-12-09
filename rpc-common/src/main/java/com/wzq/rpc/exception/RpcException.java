package com.wzq.rpc.exception;

import com.wzq.rpc.enumeration.RpcErrorMessage;

/**
 * 异常类
 *
 * @author wzq
 * @create 2022-12-02 14:59
 */
public class RpcException extends RuntimeException {

    public RpcException(RpcErrorMessage rpcErrorMessage, String detail) {
        super(rpcErrorMessage.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessage rpcErrorMessage) {
        super(rpcErrorMessage.getMessage());
    }

}
