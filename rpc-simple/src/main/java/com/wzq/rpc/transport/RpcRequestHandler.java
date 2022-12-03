package com.wzq.rpc.transport;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author wzq
 * @create 2022-12-02 17:01
 */
public class RpcRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);

    public Object handle(RpcRequest rpcRequest, Object service) {
        Object result = null;

        try {
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("service:{}, successful invoke method:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            logger.error("occur exception...", e);
        }

        return result;
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());

        if (method == null) {
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }

        return method.invoke(service, rpcRequest.getParameters());
    }

}
