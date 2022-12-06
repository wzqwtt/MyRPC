package com.wzq.rpc.serialize.kryo;

import com.wzq.rpc.remoting.dto.RpcResponse;
import com.wzq.rpc.enumeration.RpcResponseCode;
import com.wzq.rpc.serialize.Serializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * 测试KryoSerializer类
 *
 * @author wzq
 * @create 2022-12-03 14:06
 */
@Slf4j
public class TestKryoSerializer {
    
    private Serializer kryoSerializer = new KryoSerializer();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Student {
        String name;
        int age;
    }

    /**
     * 测试Kryo序列化与反序列功能
     */
    @Test
    public void testKryoSerializer() {
        // 构造一个对象
        RpcResponse<Student> rpcResponse = new RpcResponse<>();
        rpcResponse.setCode(RpcResponseCode.SUCCESS.getCode());
        rpcResponse.setMessage("success");
        rpcResponse.setData(new Student("wzq", 20));

        // 序列化
        byte[] serializeBytes = kryoSerializer.serialize(rpcResponse);
        log.info("serializeBytes length = {}", serializeBytes.length);

        // 反序列化
        RpcResponse deserialize = kryoSerializer.deserialize(serializeBytes, RpcResponse.class);
        log.info(deserialize.toString());
    }

}
