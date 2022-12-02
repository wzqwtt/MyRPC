package com.wzq.rpc;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * @author wzq
 * @create 2022-12-01 22:17
 */
public class ClientMessageHandlerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientMessageHandlerThread.class);

    private Socket socket;
    private Object service;

    public ClientMessageHandlerThread(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        try (
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())
        ) {

            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();

            // 通过反射执行方法
            Object result = invokeTargetMethod(rpcRequest);

            objectOutputStream.writeObject(RpcResponse.success(result));
            objectOutputStream.flush();

        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.error("occur exception:", e);
        }
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> cls = Class.forName(rpcRequest.getInterfaceName());

        // 判断类是否实现了对应的接口
        if (!cls.isAssignableFrom(service.getClass())) {
            return RpcResponse.fail(RpcResponseCode.NOT_FOUNT_CLASS);
        }

        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());

        if (method == null) {
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }

        return method.invoke(service, rpcRequest.getParameters());
    }

}
