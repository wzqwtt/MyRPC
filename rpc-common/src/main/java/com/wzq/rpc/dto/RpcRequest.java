package com.wzq.rpc.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * RpcRequest 请求实体类
 *
 * @author wzq
 * @create 2022-12-01 21:21
 */
@Data
@Builder
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;

    /**
     * 要调用的接口名
     */
    private String interfaceName;

    /**
     * 要调用的方法名
     */
    private String methodName;

    /**
     * 调用方法传递的参数
     */
    private Object[] parameters;

    /**
     * 调用方法的参数类型
     */
    private Class<?>[] paramTypes;

}
