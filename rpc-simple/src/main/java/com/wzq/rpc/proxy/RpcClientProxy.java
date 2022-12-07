package com.wzq.rpc.proxy;

import com.wzq.rpc.remoting.dto.RpcMessageChecker;
import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.remoting.transport.ClientTransport;
import com.wzq.rpc.remoting.transport.netty.client.NettyClientTransport;
import com.wzq.rpc.remoting.transport.socket.SocketRpcClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * RpcClient动态代理类，负责封装被调用方法的{@link RpcRequest}，并且发送请求给服务端
 *
 * @author wzq
 * @create 2022-12-01 22:17
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    /**
     * 用于发送请求给服务端，对应socket、Netty两种实现方式
     */
    private final ClientTransport clientTransport;

    public RpcClientProxy(ClientTransport clientTransport) {
        this.clientTransport = clientTransport;
    }

    /**
     * 通过 Proxy.newProxyInstance() 方法获取某个类的代理对象
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
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        log.info("invoke method: [{}]", method.getName());

        // 封装RpcRequest
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                // 设置Request ID
                .requestId(UUID.randomUUID().toString())
                .build();

        RpcResponse rpcResponse = null;

        // 发送RpcRequest请求，并返回远程调用的结果
        if (clientTransport instanceof NettyClientTransport) {
            CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) clientTransport.sendRpcRequest(rpcRequest);
            // 在这里阻塞了，等待服务端返回结果，然后交给NettyClientHandler处理，设置该completableFuture的result
            rpcResponse = completableFuture.get();
        }

        if (clientTransport instanceof SocketRpcClient) {
            rpcResponse = (RpcResponse) clientTransport.sendRpcRequest(rpcRequest);
        }
        // 校验RpcRequest和RpcResponse
        RpcMessageChecker.check(rpcResponse, rpcRequest);

        return rpcResponse.getData();
    }
}
