package com.wzq.rpc.remoting.dto;

import com.wzq.rpc.enumeration.RpcErrorMessageEnum;
import com.wzq.rpc.enumeration.RpcResponseCode;
import com.wzq.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于检查请求和响应的消息是否符合规范
 *
 * @author wzq
 * @create 2022-12-04 17:13
 */
@Slf4j
public class RpcMessageChecker {

    private static final String INTERFACE_NAME = "interfaceName";

    private RpcMessageChecker() {

    }

    /**
     * 检查消息是否合格
     *
     * @param rpcResponse RpcResponse
     * @param rpcRequest  RpcRequest
     */
    public static void check(RpcResponse rpcResponse, RpcRequest rpcRequest) {
        // 如果rpcResponse为空，则调用服务失败
        if (rpcResponse == null) {
            log.error("调用服务失败,rpcResponse为null,serviceName:{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        // 判断请求的id和响应的id是否一致。如果不一致就抛出异常
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            log.error("请求的id和响应的id不一致");
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        // 如果响应码为空，或 响应码不为SUCCESS，则调用失败
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
            log.error("调用服务失败,serviceName:{},RpcResponse:{}", rpcRequest.getInterfaceName(), rpcResponse);
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }

}
