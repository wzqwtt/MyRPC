package com.wzq.rpc.common;

import lombok.Data;

/**
 * 封装的响应对象
 *
 * @author wzq
 * @create 2022-11-28 14:41
 */
@Data
public class RpcResponse {

    /**
     * 响应ID
     */
    private String requestId;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 返回的结果
     */
    private Object result;

}
