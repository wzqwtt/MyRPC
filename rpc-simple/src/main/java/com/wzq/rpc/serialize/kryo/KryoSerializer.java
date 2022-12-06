package com.wzq.rpc.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.wzq.rpc.remoting.dto.RpcRequest;
import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.exception.SerializeException;
import com.wzq.rpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo序列化与反序列化
 *
 * @author wzq
 * @create 2022-12-02 22:20
 */
@Slf4j
public class KryoSerializer implements Serializer {
    
    /**
     * 由于Kryo不是线程安全的。每个线程都应该有自己的Kryo，Input和Output实例
     * 所以，使用ThreadLocal存放Kryo对象
     */
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();

        // 注册两个类
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);

        // 默认值为true，是否关闭注册行为，关闭之后可能存在序列化问题，一般推荐设置为true
        kryo.setReferences(true);

        // 默认值为false，是否关闭循环引用，可以提高性能，但是一般不推荐设置为true
        kryo.setRegistrationRequired(false);

        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Output output = new Output(byteArrayOutputStream);
        ) {
            Kryo kryo = kryoThreadLocal.get();
            // Object -> byte[]：将对象序列化为byte数组
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            log.error("occur exception when serialize:", e);
            throw new SerializeException("序列化失败！");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                Input input = new Input(byteArrayInputStream);
        ) {
            Kryo kryo = kryoThreadLocal.get();
            Object obj = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(obj);
        } catch (Exception e) {
            log.error("occur exception when deserialize: ", e);
            throw new SerializeException("反序列化失败！");
        }
    }
}
