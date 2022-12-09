package com.wzq.rpc.remoting.transport.netty.codec.kryo;

import com.wzq.rpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty序列化编码器
 *
 * @author wzq
 * @create 2022-12-03 16:28
 */
@AllArgsConstructor
@Slf4j
public class NettySerializerEncoder extends MessageToByteEncoder {
    
    /**
     * 序列化器
     */
    private final Serializer serializer;

    /**
     * 通用类
     */
    private final Class<?> genericClass;

    /**
     * 将对象转换为字节码然后写入到ByteBuf中
     *
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (genericClass.isInstance(msg)) {
            // 将消息序列化为为genericClass类型
            byte[] body = serializer.serialize(msg);
            // 消息长度
            int bodyLength = body.length;
            // 将消息写入ByteBuf
            out.writeInt(bodyLength);
            out.writeBytes(body);
        }
    }
}
