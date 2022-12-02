package com.wzq.rpc;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcErrorMessageEnum;
import com.wzq.rpc.enumeration.RpcResponseCode;
import com.wzq.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author wzq
 * @create 2022-12-01 21:25
 */
public class RpcClient {

    public static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    public Object sendRpcRequest(RpcRequest rpcRequest, String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();

            if (rpcResponse == null) {
                logger.error("调用服务失败,serviceName:{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE,
                        "interfaceName:" + rpcRequest.getInterfaceName());
            }

            if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
                logger.error("调用服务失败,serviceName:{},RpcRespnse:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE,
                        "interfaceName:" + rpcRequest.getInterfaceName());
            }

            return rpcResponse.getData();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException("调用服务失败:", e);
        }

    }

}
