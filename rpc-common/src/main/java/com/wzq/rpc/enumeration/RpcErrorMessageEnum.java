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

    // 没有找到指定的服务
    SERVICE_CAN_NOT_FOUND("没有找到指定的服务"),

    // 注册的服务没有实现任何接口
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务没有实现任何接口");

    private final String message;

}
