package com.wzq.rpc.remoting.transport.netty.server;

import com.wzq.rpc.enumeration.RpcMessageTypeEnum;
import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.handler.RpcRequestHandler;
import com.wzq.rpc.factory.SingletonFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端入站Handler，处理RpcRequest
 *
 * @author wzq
 * @create 2022-12-03 21:42
 */
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 专门处理Rpc请求的处理器
     */
    private final RpcRequestHandler rpcRequestHandler;

    public NettyServerHandler() {
        // 通过单例工厂获得RpcRequest类，用于反射调用RpcRequest里的方法
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 此时的msg经过解码器处理，已经是RpcRequest了
        try {
            // 转换消息为RpcRequest类型
            RpcRequest rpcRequest = (RpcRequest) msg;
            log.info("server receive msg: {}", rpcRequest);

            if (rpcRequest.getRpcMessageTypeEnum() == RpcMessageTypeEnum.HEART_BEAT) {
                log.info("receive heat beat msg from client");
                return;
            }

            // 调用RpcRequestHandler处理请求，反射调用方法并返回结果
            Object result = rpcRequestHandler.handle(rpcRequest);
            log.info("server get result: {}", result.toString());

            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                // 返回方法执行结果给客户端
                RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                log.error("not writable now, message dropped");
            }
        } finally {
            // 释放资源
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception...");
        cause.printStackTrace();
        ctx.close();
    }
}
