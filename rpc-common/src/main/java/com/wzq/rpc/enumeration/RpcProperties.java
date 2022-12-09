package com.wzq.rpc.enumeration;

/**
 * @author wzq
 * @create 2022-12-09 19:32
 */
public enum RpcProperties {

    /**
     * RPC配置路径
     */
    RPC_CONFIG_PATH("rpc.properties"),

    /**
     * 配置Zookeeper地址的前缀名
     */
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;

    RpcProperties(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyValue() {
        return propertyValue;
    }
}
