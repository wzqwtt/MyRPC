package com.wzq.rpc.rpcconsumer.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * Netty Handler 客户端业务处理类
 *
 * @author wzq
 * @create 2022-11-29 16:50
 */
@Slf4j
@Component
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<String> implements Callable {

    ChannelHandlerContext context = null;

    /**
     * 发送消息
     */
    private String reqMsg;

    /**
     * 服务端返回的消息
     */
    private String respMsg;

    /**
     * 设置reqMsg
     *
     * @param reqMsg
     */
    public void setReqMsg(String reqMsg) {
        this.reqMsg = reqMsg;
    }

    /**
     * 通道读取就绪事件，读取服务端消息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected synchronized void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 服务器返回的数据
        respMsg = msg;
        // 唤醒等待线程
        notify();
    }

    /**
     * 通道连接就绪事件，不能通道连接之后就发送消息
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 当通道连接就绪给ctx赋值
        context = ctx;
    }

    /**
     * 给服务端发送消息
     *
     * @return
     * @throws Exception
     */
    @Override
    public synchronized Object call() throws Exception {
        // 调用call方法发送请求
        context.writeAndFlush(reqMsg);
        // 等待服务端返回数据，将线程置于等待状态
        wait();
        return respMsg;
    }
}
