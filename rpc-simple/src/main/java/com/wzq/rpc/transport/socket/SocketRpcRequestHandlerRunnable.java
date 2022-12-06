package com.wzq.rpc.transport.socket;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.handler.RpcRequestHandler;
import com.wzq.rpc.utils.factory.SingletonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 处理客户端传递过来的RpcRequest信息线程，在这个类中，仅读取入站数据，
 * 反射调用方法由类{@link RpcRequestHandler}完成。最后把方法结果传回客户端
 *
 * @author wzq
 * @create 2022-12-02 16:33
 */
public class SocketRpcRequestHandlerRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SocketRpcRequestHandlerRunnable.class);

    /**
     * 客户端来的Socket
     */
    private Socket socket;

    /**
     * 真正处理RpcRequest的类
     */
    private RpcRequestHandler rpcRequestHandler;

    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
        // 通过单例工厂获得RpcRequest类，用于反射调用RpcRequest里的方法
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    /**
     * 作为一个线程执行任务
     */
    @Override
    public void run() {
        logger.info("server handler message from client by thread {}", Thread.currentThread().getName());
        try (
                // 入站流对象
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                // 出站流对象
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ) {
            // 读取Socket里面的RpcRequest
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();

            // 由handler进行处理
            Object result = rpcRequestHandler.handle(rpcRequest);

            // 写数据到客户端
            objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur exception:", e);
        }
    }
}
