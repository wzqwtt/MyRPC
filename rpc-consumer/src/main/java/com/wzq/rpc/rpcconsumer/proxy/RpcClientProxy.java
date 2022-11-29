package com.wzq.rpc.rpcconsumer.proxy;

import com.alibaba.fastjson.JSON;
import com.wzq.rpc.common.RpcRequest;
import com.wzq.rpc.common.RpcResponse;
import com.wzq.rpc.rpcconsumer.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RpcClient代理对象，自动封装RpcRequest对象
 *
 * @author wzq
 * @create 2022-11-29 17:10
 */
@Slf4j
@Component
public class RpcClientProxy {

    /**
     * 缓存
     */
    Map<Class, Object> SERVICE_PROXY = new HashMap<>();

    @Autowired
    NettyRpcClient rpcClient;

    /**
     * 获取代理对象
     *
     * @param serviceClass
     * @return
     */
    public Object getProxy(Class serviceClass) {
        // 从缓存中查找对象
        Object proxy = SERVICE_PROXY.get(serviceClass);

        // 如果没有查找到，就创建代理对象，并且放入缓存
        if (proxy == null) {
            // 创建代理对象
            proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{serviceClass},
                    // 在invoke方法中封装RpcRequest对象，并且发送请求
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            // 封装请求对象
                            RpcRequest rpcRequest = new RpcRequest();
                            rpcRequest.setRequestId(UUID.randomUUID().toString());
                            rpcRequest.setClassName(method.getDeclaringClass().getName());
                            rpcRequest.setMethodName(method.getName());
                            rpcRequest.setParameterTypes(method.getParameterTypes());
                            rpcRequest.setParameters(args);

                            log.debug("封装消息: {}", rpcRequest);

                            // 发送消息
                            Object msg = rpcClient.send(JSON.toJSONString(rpcRequest));

                            // 将消息转化
                            RpcResponse rpcResponse = JSON.parseObject(msg.toString(), RpcResponse.class);

                            if (rpcResponse.getError() != null) {
                                throw new RuntimeException(rpcResponse.getError());
                            }
                            if (rpcResponse.getResult() != null) {
                                return JSON.parseObject(rpcResponse.getResult().toString(),
                                        method.getReturnType());
                            }

                            return null;
                        }
                    });

            // 放入缓存
            SERVICE_PROXY.put(serviceClass, proxy);
        }

        return proxy;
    }

}
