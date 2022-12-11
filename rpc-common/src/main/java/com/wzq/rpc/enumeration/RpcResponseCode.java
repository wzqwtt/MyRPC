package com.wzq.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * RpcResponse响应码
 *
 * @author wzq
 * @create 2022-12-02 14:50
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCode {

    // SUCCESS响应成功
    SUCCESS(200, "远程方法调用成功"),

    // FAIL调用方法失败
    FAIL(500, "远程方法调用失败");

    private final int code;
    private final String message;

}
