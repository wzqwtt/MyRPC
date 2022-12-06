package com.wzq.rpc.handler;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcResponseCode;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.provider.ServiceProvider;
import com.wzq.rpc.provider.ServiceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射调用RpcRequest中请求的方法，最后返回结果
 *
 * @author wzq
 * @create 2022-12-02 17:01
 */
public class RpcRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);

    /**
     * 注册中心
     */
    private static final ServiceProvider serviceProvider = new ServiceProviderImpl();

    /**
     * 处理RpcRequest中的请求
     *
     * @param rpcRequest 客户端发送的RpcRequest
     * @return 方法调用的结果
     */
    public Object handle(RpcRequest rpcRequest) {
        Object result = null;

        // 通过Provider获取目标类（即客户端需要调用的类）
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());

        // 反射调用方法
        result = invokeTargetMethod(rpcRequest, service);
        logger.info("service:{}, successful invoke method:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());

        return result;
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

            // 如果方法为空，则返回调用失败
            if (method == null) {
                return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
            }

            // 反射调用方法
            result = method.invoke(service, rpcRequest.getParameters());
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }

        return result;
    }

}
