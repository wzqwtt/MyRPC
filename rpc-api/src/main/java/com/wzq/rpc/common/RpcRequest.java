package com.wzq.rpc.common;

import lombok.Data;

/**
 * 封装的请求对象
 *
 * @author wzq
 * @create 2022-11-28 14:41
 */
@Data
public class RpcRequest {

    /**
     * 请求对象的ID
     */
    private String requestId;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;

    /**
     * 入参
     */
    private Object[] parameters;

}
