package com.wzq.rpc.transport;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcResponseCode;
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
     * 处理RpcRequest中的请求
     *
     * @param rpcRequest 客户端发送的RpcRequest
     * @param service    服务
     * @return 方法调用的结果
     */
    public Object handle(RpcRequest rpcRequest, Object service) {
        Object result = null;

        try {
            // 反射调用方法
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("service:{}, successful invoke method:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            logger.error("occur exception...", e);
        }

        return result;
    }

    /**
     * 反射调用方法
     *
     * @param rpcRequest 请求体
     * @param service    服务
     * @return 方法调用的结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 获取方法
        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());

        // 如果方法为空，则返回调用失败
        if (method == null) {
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }

        // 反射调用方法
        return method.invoke(service, rpcRequest.getParameters());
    }

}
