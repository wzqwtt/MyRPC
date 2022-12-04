package com.wzq.rpc.transport;

import com.wzq.rpc.dto.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RpcClient动态代理类，负责封装被调用方法的{@link RpcRequest}，并且发送请求给服务端
 *
 * @author wzq
 * @create 2022-12-01 22:17
 */
public class RpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    /**
     * RpcClient属性
     */
    private RpcClient rpcClient;

    public RpcClientProxy(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    /**
     * 获取代理
     *
     * @param clazz 返回服务的代理
     * @param <T>   某个接口的class对象
     * @return 返回T的代理类
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        // 返回动态代理实例
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                this
        );
    }

    /**
     * 重写的invoke方法，负责封装被调用方法的{@link RpcRequest}，并且发送请求给服务端。
     * 当动态代理实例调用方法的时候，首先会调用这个方法
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        logger.info("Call invoke method and invoked method: {}", method.getName());

        // 封装RpcRequest
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                // 设置Request ID
                .requestId(UUID.randomUUID().toString())
                .build();

        // 发送RpcRequest请求，并返回远程调用的结果
        return rpcClient.sendRpcRequest(rpcRequest);
    }
}
