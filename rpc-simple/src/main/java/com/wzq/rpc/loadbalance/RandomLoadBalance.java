package com.wzq.rpc.loadbalance;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡
 *
 * @author wzq
 * @create 2022-12-07 22:44
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
