package com.wzq.rpc.exception;

import com.wzq.rpc.enumeration.RpcErrorMessageEnum;

/**
 * 异常类
 *
 * @author wzq
 * @create 2022-12-02 14:59
 */
public class RpcException extends RuntimeException {

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }

}
