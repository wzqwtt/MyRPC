package com.wzq.rpc.remoting.dto;

import com.wzq.rpc.enumeration.RpcMessageType;
import lombok.*;

import java.io.Serializable;

/**
 * RpcRequest 请求实体类
 *
 * @author wzq
 * @create 2022-12-01 21:21
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;

    /**
     * RpcRequest ID
     */
    private String requestId;

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

    /**
     * Rpc消息类型
     */
    private RpcMessageType rpcMessageType;

    /**
     * 服务的version
     */
    private String version;

    /**
     * 该服务隶属于哪个组
     */
    private String group;

}
