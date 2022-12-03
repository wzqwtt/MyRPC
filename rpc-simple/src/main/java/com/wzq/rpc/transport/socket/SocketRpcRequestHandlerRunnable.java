package com.wzq.rpc.transport.socket;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.registry.DefaultServiceRegistry;
import com.wzq.rpc.registry.ServiceRegistry;
import com.wzq.rpc.transport.RpcRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author wzq
 * @create 2022-12-02 16:33
 */
public class SocketRpcRequestHandlerRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SocketRpcRequestHandlerRunnable.class);

    private Socket socket;
    private static RpcRequestHandler rpcRequestHandler;
    private static ServiceRegistry serviceRegistry;

    static {
        rpcRequestHandler = new RpcRequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
    }

    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ) {
            // 读取Socket里面的RpcRequest
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();

            // 获取接口名称，并且在“缓存中查找对应的service”，最后由handler进行处理
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result = rpcRequestHandler.handle(rpcRequest, service);

            // 写数据到客户端
            objectOutputStream.writeObject(RpcResponse.success(result));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur exception:", e);
        }
    }
}