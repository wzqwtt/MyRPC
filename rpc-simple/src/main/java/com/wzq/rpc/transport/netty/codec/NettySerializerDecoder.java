package com.wzq.rpc.transport.netty.codec;

import com.wzq.rpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Netty序列化解码器
 *
 * @author wzq
 * @create 2022-12-03 16:19
 */
@AllArgsConstructor
@Slf4j
public class NettySerializerDecoder extends ByteToMessageDecoder {
    
    /**
     * 序列化器
     */
    private Serializer serializer;

    /**
     * 通用类
     */
    private Class<?> genericClass;

    /**
     * Netty传输的消息长度也就是对象序列化后对应的字节数组大小，存储在ByteBuf头部
     */
    private static final int BODY_LENGTH = 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // byteBuf中写入的消息长度所占的字节数已经是4了所以byteBuf的可读字节必须大于4
        if (in.readableBytes() >= BODY_LENGTH) {
            // 标记当前readIndex位置，以便后面重置readIndex的时候用
            in.markReaderIndex();

            // 读取消息的长度，在编码的时候写入了4个字节的长度
            int dataLength = in.readInt();

            // 如果dataLength不合理，直接return
            if (dataLength < 0 || in.readableBytes() < 0) {
                return;
            }

            // 如果可读字节数小于消息长度，说明是不完整消息，重置readIndex并返回
            if (in.readableBytes() < dataLength) {
                in.resetReaderIndex();
                return;
            }

            // 走到这里代表可以读取数据了
            byte[] body = new byte[dataLength];
            in.readBytes(body);
            Object obj = serializer.deserialize(body, genericClass);

            // 将读取出来的object加入List，传入下一个Handler
            out.add(obj);
        }
    }
}
