package com.wzq.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author wzq
 * @create 2022-12-02 14:57
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcErrorMessageEnum {

    // 服务调用失败响应消息
    SERVICE_INVOCATION_FAILURE("服务调用失败"),

    // 注册服务不能为空响应消息
    SERVICE_CAN_NOT_BE_NULL("注册的服务不能为空");

    private final String message;

}
