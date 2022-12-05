package com.wzq.rpc.transport.socket;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.transport.ClientTransport;
import com.wzq.rpc.utils.checker.RpcMessageChecker;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Socket Rpc Client 使用Socket编写的Rpc客户端
 *
 * @author wzq
 * @create 2022-12-01 21:25
 */
@AllArgsConstructor
public class SocketClientTransport implements ClientTransport {

    private static final Logger logger = LoggerFactory.getLogger(SocketClientTransport.class);

    /**
     * 主机和端口
     */
    private String host;
    private int port;

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 新建一个Socket
        try (Socket socket = new Socket(host, port)) {
            // 发送RpcRequest到服务端
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);

            // 阻塞接收客户端响应
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // 从流中读出RpcResponse
            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();

            // 检查数据是否合格
            RpcMessageChecker.check(rpcResponse, rpcRequest);

            // 返回响应的结果
            return rpcResponse.getData();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur exception when send RpcRequest");
            throw new RpcException("调用服务失败:", e);
        }

    }

}