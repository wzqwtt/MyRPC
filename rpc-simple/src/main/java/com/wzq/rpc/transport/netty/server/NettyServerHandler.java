package com.wzq.rpc.transport.netty.server;

import com.wzq.rpc.dto.RpcRequest;
import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcErrorMessageEnum;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.handler.RpcRequestHandler;
import com.wzq.rpc.utils.concurrent.ThreadPoolFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

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
    private static final RpcRequestHandler rpcRequestHandler;

    /**
     * 线程池
     */
    private static final ExecutorService threadPool;

    /**
     * 自定义的线程池前缀名
     */
    private static final String THREAD_NAME_PREFIX = "netty-server-handler-rpc-pool";

    static {
        rpcRequestHandler = new RpcRequestHandler();
        // 获取线程池
        threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 使用一个线程专门执行任务
        threadPool.execute(() -> {
            logger.info("server handle message from client by thread: {}", Thread.currentThread().getName());
            // 此时的msg经过解码器处理，已经是RpcRequest了
            if (msg instanceof RpcRequest) {
                try {
                    // 转换消息为RpcRequest类型
                    RpcRequest rpcRequest = (RpcRequest) msg;
                    logger.info("server receive msg: {}", rpcRequest);

                    // 调用RpcRequestHandler处理请求，反射调用方法并返回结果
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    logger.info("server get result: {}", result.toString());

                    // 将结果封装为RpcResponse发到客户端
                    ChannelFuture f = ctx.writeAndFlush(RpcResponse.success(result, rpcRequest.getRequestId()));
                    f.addListener(ChannelFutureListener.CLOSE);
                } finally {
                    // 释放资源
                    ReferenceCountUtil.release(msg);
                }
            } else {
                logger.error("不合法的消息被传递");
                throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, "服务端错误");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server catch exception...");
        cause.printStackTrace();
        ctx.close();
    }
}
