package com.wzq.rpc.loadbalance;

import java.util.List;

/**
 * 负载均衡抽象类
 *
 * @author wzq
 * @create 2022-12-07 22:42
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public String selectServiceAddress(List<String> serviceAddresses) {
        if (serviceAddresses == null || serviceAddresses.size() == 0) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses);
    }

    protected abstract String doSelect(List<String> serviceAddresses);
}
