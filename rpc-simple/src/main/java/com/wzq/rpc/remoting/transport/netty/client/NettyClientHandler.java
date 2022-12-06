package com.wzq.rpc.remoting.transport.netty.client;

import com.wzq.rpc.factory.SingletonFactory;
import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcErrorMessageEnum;
import com.wzq.rpc.exception.RpcException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义的Netty Client入站处理器
 *
 * @author wzq
 * @create 2022-12-03 21:24
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedRequests unprocessedRequests;

    public NettyClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    /**
     * 读取管道中的数据
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 此处的Client入站处理器，处理的是RpcResponse
        // 在上一步的解码器中，已经解码为RpcResponse类型，再判断一下
        if (msg instanceof RpcResponse) {
            try {
                log.info("client receive msg: {}", msg);
                RpcResponse rpcResponse = (RpcResponse) msg;
                unprocessedRequests.complete(rpcResponse);
            } finally {
                // 释放msg资源
                ReferenceCountUtil.release(msg);
            }
        } else {
            log.error("不合法的消息被传递");
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, "不合法的消息被传递");
        }
    }

    /**
     * 处理Netty管道中的异常
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
