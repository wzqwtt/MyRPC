package com.wzq.rpc.remoting.transport.socket;

import com.wzq.rpc.entity.RpcServiceProperties;
import com.wzq.rpc.extension.ExtensionLoader;
import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.registry.ServiceDiscovery;
import com.wzq.rpc.registry.zk.ZkServiceDiscovery;
import com.wzq.rpc.remoting.transport.ClientTransport;
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
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");

    }

    /**
     * 负责发送RpcRequest，并返回远程过程调用的结果
     *
     * @param rpcRequest 封装的RpcRequest消息
     * @return 返回远程过程调用的结果
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 获取请求中的rpcServiceName
        String rpcServiceName = RpcServiceProperties.builder()
                .serviceName(rpcRequest.getInterfaceName())
                .group(rpcRequest.getGroup())
                .version(rpcRequest.getVersion())
                .build()
                .toRpcServiceName();
        // 获取service的地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);

        // 新建一个Socket
        try (Socket socket = new Socket()) {
            // 连接到服务所在的主机
            socket.connect(inetSocketAddress);
            // 发送RpcRequest到服务端
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);

            // 阻塞接收客户端响应
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            // 返回响应的结果
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("occur exception when send RpcRequest");
            throw new RpcException("调用服务失败:", e);
        }

    }

}
