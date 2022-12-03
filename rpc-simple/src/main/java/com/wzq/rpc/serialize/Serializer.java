package com.wzq.rpc.serialize;

/**
 * 序列化与反序列化机制，所有序列化类都需要实现这个接口
 *
 * @author wzq
 * @create 2022-12-02 22:18
 */
public interface Serializer {

    /**
     * 序列化
     *
     * @param obj 要序列化的对象
     * @return 字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     *
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @param <T>   任意类型
     * @return 反序列化的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

}
