package com.wzq.rpc.loadbalance;

import java.util.List;

/**
 * 负载均衡接口
 *
 * @author wzq
 * @create 2022-12-07 22:41
 */
public interface LoadBalance {

    /**
     * 在已有服务提供地址列表中选择一个
     *
     * @param serviceAddresses 服务地址列表
     * @return 目标服务地址
     */
    String selectServiceAddress(List<String> serviceAddresses);

}
