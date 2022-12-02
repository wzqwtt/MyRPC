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
    SUCCESS(200, "调用方法成功"),

    // FAIL调用方法失败
    FAIL(500, "调用方法失败"),

    // 没有找到方法
    NOT_FOUND_METHOD(500, "未找到指定方法"),

    // 没有找到类
    NOT_FOUNT_CLASS(500, "未找到指定类");

    private final int code;
    private final String message;

}
