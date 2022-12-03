package com.wzq.rpc.transport.netty.server;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcErrorMessageEnum;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.registry.DefaultServiceRegistry;
import com.wzq.rpc.registry.ServiceRegistry;
import com.wzq.rpc.transport.RpcRequestHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端入站Handler，处理RpcRequest
 *
 * @author wzq
 * @create 2022-12-03 21:42
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    /**
     * 专门处理Rpc请求的处理器
     */
    private static RpcRequestHandler rpcRequestHandler;

    /**
     * 注册中心
     */
    private static ServiceRegistry serviceRegistry;

    static {
        rpcRequestHandler = new RpcRequestHandler();
        // TODO 注册中心的默认实现改为配置文件的方式
        serviceRegistry = new DefaultServiceRegistry();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 此时的msg经过解码器处理，已经是RpcRequest了
        if (msg instanceof RpcRequest) {
            try {
                // 转换消息为RpcRequest类型
                RpcRequest rpcRequest = (RpcRequest) msg;
                logger.info(String.format("server reveive msg: %s", rpcRequest));
                // 去注册中心找到请求体中接口对应的服务
                Object service = serviceRegistry.getService(rpcRequest.getInterfaceName());
                // 调用RpcRequestHandler处理请求，反射调用方法并返回结果
                Object result = rpcRequestHandler.handle(rpcRequest, service);
                logger.info(String.format("server get result: %s", result.toString()));

                // 将结果封装为RpcResponse发到客户端
                ChannelFuture f = ctx.writeAndFlush(RpcResponse.success(result));
                f.addListener(ChannelFutureListener.CLOSE);
            } finally {
                // 释放资源
                ReferenceCountUtil.release(msg);
            }
        } else {
            logger.error("不合法的消息被传递");
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, "服务端错误");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server catchexception...");
        cause.printStackTrace();
        ctx.close();
    }
}
