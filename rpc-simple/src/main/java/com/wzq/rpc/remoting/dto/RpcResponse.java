package com.wzq.rpc.remoting.dto;

import com.wzq.rpc.enumeration.RpcResponseCode;
import lombok.*;

import java.io.Serializable;

/**
 * RpcResponse 响应实体类
 *
 * @author wzq
 * @create 2022-12-02 14:46
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;

    /**
     * RpcRequest ID
     */
    private String requestId;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 设置成功响应消息
     *
     * @param data 远程调用的返回数据
     * @param <T>  响应数据泛型
     * @return 返回一个RpcResponse
     */
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCode.SUCCESS.getCode());
        response.setMessage(RpcResponseCode.SUCCESS.getMessage());
        response.setRequestId(requestId);

        if (data != null) {
            response.setData(data);
        }

        return response;
    }

    /**
     * 设置失败的响应消息
     *
     * @param rpcResponseCode 响应码
     * @param <T>             响应数据泛型
     * @return 返回一个RpcResponse
     */
    public static <T> RpcResponse<T> fail(RpcResponseCode rpcResponseCode) {
        RpcResponse<T> response = new RpcResponse<>();

        response.setCode(rpcResponseCode.getCode());
        response.setMessage(rpcResponseCode.getMessage());
        // TODO(requestID)
        return response;
    }

}
