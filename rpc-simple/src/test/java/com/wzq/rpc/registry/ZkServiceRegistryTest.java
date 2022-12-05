package com.wzq.rpc.registry;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author wzq
 * @create 2022-12-05 19:30
 */
public class ZkServiceRegistryTest {

    @Test
    public void should_register_service_successful_and_lookup_service_by_service_name() {
        ZkServiceRegistry zkServiceRegistry = new ZkServiceRegistry();
        InetSocketAddress givenInetSocketAddress = new InetSocketAddress("127.0.0.1", 9333);
        String serviceName = "com.wzq.registry.ZkServiceRegistry";
        zkServiceRegistry.registerService(serviceName, givenInetSocketAddress);

        InetSocketAddress inetSocketAddress = zkServiceRegistry.lookupService(serviceName);
        System.out.println(inetSocketAddress);
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
