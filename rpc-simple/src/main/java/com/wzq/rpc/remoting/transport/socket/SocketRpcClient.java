package com.wzq.rpc.remoting.transport.socket;

import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.registry.ServiceDiscovery;
import com.wzq.rpc.registry.ZkServiceDiscovery;
import com.wzq.rpc.remoting.transport.ClientTransport;
import com.wzq.rpc.remoting.dto.RpcMessageChecker;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Socket Rpc Client 使用Socket编写的Rpc客户端
 *
 * @author wzq
 * @create 2022-12-01 21:25
 */
@Slf4j
public class SocketRpcClient implements ClientTransport {
    
    /**
     * 服务发现
     */
    private final ServiceDiscovery serviceDiscovery;

    /**
     * 无参构造方法，默认创建一个Zookeeper的注册中心
     */
    public SocketRpcClient() {
        this.serviceDiscovery = new ZkServiceDiscovery();

    }

    /**
     * 负责发送RpcRequest，并返回远程过程调用的结果
     *
     * @param rpcRequest 封装的RpcRequest消息
     * @return 返回远程过程调用的结果
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 获取rpcRequest方法的接口名，并在注册中心寻找实现类所在主机和端口号
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        // 新建一个Socket
        try (Socket socket = new Socket()) {
            // 连接到服务所在的主机
            socket.connect(inetSocketAddress);
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
            return rpcResponse;
        } catch (IOException | ClassNotFoundException e) {
            log.error("occur exception when send RpcRequest");
            throw new RpcException("调用服务失败:", e);
        }

    }

}
