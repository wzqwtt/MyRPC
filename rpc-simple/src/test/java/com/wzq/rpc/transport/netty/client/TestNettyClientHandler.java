package com.wzq.rpc.transport.netty.client;

import com.wzq.rpc.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcResponseCode;
import com.wzq.rpc.serialize.Serializer;
import com.wzq.rpc.serialize.kryo.KryoSerializer;
import com.wzq.rpc.transport.netty.codec.TestNettySerializerCodec;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author wzq
 * @create 2022-12-03 21:37
 */
@Slf4j
public class TestNettyClientHandler {
    
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
        rpcResponse.setData(new TestNettySerializerCodec.Student("wzq", 20));
    }

    /**
     * 测试Handler
     */
    @Test
    public void testClientHandler() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                // 自定义的ClientHandler
                new NettyClientHandler()
        );

        embeddedChannel.writeInbound(rpcResponse);
    }

}
