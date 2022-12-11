package com.wzq.rpc.entity;

import lombok.*;

/**
 * @author wzq
 * @create 2022-12-09 20:31
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceProperties {

    /**
     * service version
     */
    private String version;

    /**
     * 当接口有多个实现类时，按组区分
     */
    private String group;

    /**
     * service Name
     */
    private String serviceName;

    public String toRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

}
