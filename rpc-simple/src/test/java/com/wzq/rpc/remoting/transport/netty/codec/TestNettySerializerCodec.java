package com.wzq.rpc.remoting.transport.netty.codec;

import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcResponseCode;
import com.wzq.rpc.serialize.Serializer;
import com.wzq.rpc.serialize.kryo.KryoSerializer;
import com.wzq.rpc.remoting.transport.netty.codec.kryo.NettySerializerDecoder;
import com.wzq.rpc.remoting.transport.netty.codec.kryo.NettySerializerEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * {@link NettySerializerEncoder}与{@link NettySerializerDecoder}测试类
 *
 * @author wzq
 * @create 2022-12-03 16:56
 */
@Slf4j
public class TestNettySerializerCodec {
    
    /**
     * 序列化器
     */
    private final Serializer serializer = new KryoSerializer();

    /**
     * RpcResponse对象，用于测试
     */
    private static RpcResponse rpcResponse;

    static {
        // 构建一个RpcResponse
        rpcResponse = new RpcResponse<>();
        rpcResponse.setCode(RpcResponseCode.SUCCESS.getCode());
        rpcResponse.setMessage("success");
        rpcResponse.setData(new Student("wzq", 20));
    }

    /**
     * 测试编码器
     */
    @Test
    public void testEncoder() {
        // 使用EmbeddedChannel测试Handler
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                // log
                new LoggingHandler(LogLevel.DEBUG),
                // 编码器
                new NettySerializerEncoder(serializer, RpcResponse.class)
        );

        // 手动序列化rpcResponse
        byte[] serialize = serializer.serialize(rpcResponse);
        log.debug("data: {}", serialize);
        log.debug("data length: {}", serialize.length);

        // 发送出站数据到EmbeddedChannel
        embeddedChannel.writeOutbound(rpcResponse);
    }

    /**
     * 测试解码器
     */
    @Test
    public void testDecoder() {
        // 使用EmbeddedChannel测试Handler
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                // 使用一个入站Handler模拟数据
                new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        // 将消息编码
                        NettySerializerEncoder encoder = new NettySerializerEncoder(serializer, RpcResponse.class);
                        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
                        // encoder.encode(ctx, msg, buf);
                        // 打印ByteBuf中的内容
                        log(buf);
                        // 将编码好的消息发送到下游入站处理器
                        super.channelRead(ctx, buf);
                    }
                },
                // 解码器
                new NettySerializerDecoder(serializer, RpcResponse.class),
                // 再使用一个入站处理器，查看输出的结果
                new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        log.debug("类型: {}", msg.getClass());
                        log.debug("{}", msg);
                    }
                }
        );

        // 发送入站消息到rpcResponse
        embeddedChannel.writeInbound(rpcResponse);
    }



    @NoArgsConstructor
    @AllArgsConstructor
    public static class Student {
        private String name;
        private int age;
    }

    /**
     * 打印ByteBuf
     *
     * @param buffer
     */
    public static void log(ByteBuf buffer) {
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(buffer.readerIndex())
                .append(" write index:").append(buffer.writerIndex())
                .append(" capacity:").append(buffer.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(buf, buffer);
        System.out.println(buf.toString());
    }

}
