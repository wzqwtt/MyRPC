package com.wzq.rpc.remoting.handler;

import com.wzq.rpc.entity.RpcServiceProperties;
import com.wzq.rpc.factory.SingletonFactory;
import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.provider.ServiceProvider;
import com.wzq.rpc.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射调用RpcRequest中请求的方法，最后返回结果
 *
 * @author wzq
 * @create 2022-12-02 17:01
 */
@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    /**
     * 处理RpcRequest中的请求
     *
     * @param rpcRequest 客户端发送的RpcRequest
     * @return 方法调用的结果
     */
    public Object handle(RpcRequest rpcRequest) {
        // 构造一个RpcServiceProperties
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .serviceName(rpcRequest.getInterfaceName())
                .version(rpcRequest.getVersion())
                .group(rpcRequest.getGroup())
                .build();

        // 通过Provider获取目标类（即客户端需要调用的类）
        Object service = serviceProvider.getServiceProvider(rpcServiceProperties);
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 根据 rpcRequest 和 service 对象特定的方法并返回结果
     *
     * @param rpcRequest 客户端发过来的请求
     * @param service    提供服务的对象
     * @return 方法调用的结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;

        try {
            // 获取方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());

            // 反射调用方法
            result = method.invoke(service, rpcRequest.getParameters());
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }

        return result;
    }

}
