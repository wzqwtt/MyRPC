package com.wzq.rpc.remoting.transport.netty.server;

import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcErrorMessageEnum;
import com.wzq.rpc.exception.RpcException;
import com.wzq.rpc.handler.RpcRequestHandler;
import com.wzq.rpc.utils.concurrent.ThreadPoolFactory;
import com.wzq.rpc.utils.factory.SingletonFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

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

    /**
     * 线程池
     */
    private final ExecutorService threadPool;

    /**
     * 自定义的线程池前缀名
     */
    private static final String THREAD_NAME_PREFIX = "netty-server-handler-rpc-pool";

    public NettyServerHandler() {
        // 通过单例工厂获得RpcRequest类，用于反射调用RpcRequest里的方法
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
        // 获取线程池
        this.threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 使用一个线程专门执行任务
        threadPool.execute(() -> {
            log.info("server handle message from client by thread: {}", Thread.currentThread().getName());
            // 此时的msg经过解码器处理，已经是RpcRequest了
            if (msg instanceof RpcRequest) {
                try {
                    // 转换消息为RpcRequest类型
                    RpcRequest rpcRequest = (RpcRequest) msg;
                    log.info("server receive msg: {}", rpcRequest);

                    // 调用RpcRequestHandler处理请求，反射调用方法并返回结果
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info("server get result: {}", result.toString());

                    // 将结果封装为RpcResponse发到客户端
                    ChannelFuture f = ctx.writeAndFlush(RpcResponse.success(result, rpcRequest.getRequestId()));
                    f.addListener(ChannelFutureListener.CLOSE);
                } finally {
                    // 释放资源
                    ReferenceCountUtil.release(msg);
                }
            } else {
                log.error("不合法的消息被传递");
                throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, "服务端错误");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception...");
        cause.printStackTrace();
        ctx.close();
    }
}
