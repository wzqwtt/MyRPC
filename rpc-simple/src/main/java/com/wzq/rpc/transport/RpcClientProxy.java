package com.wzq.rpc.transport;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.transport.socket.SocketRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author wzq
 * @create 2022-12-01 22:17
 */
public class RpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    private RpcClient rpcClient;

    public RpcClientProxy(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                this
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        logger.info("Call invoke method and invoked method: {}", method.getName());

        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .build();

        return rpcClient.sendRpcRequest(rpcRequest);
    }
}
