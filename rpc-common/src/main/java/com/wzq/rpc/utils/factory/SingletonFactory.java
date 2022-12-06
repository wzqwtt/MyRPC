package com.wzq.rpc.utils.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取单例对象的工厂类
 *
 * @author wzq
 * @create 2022-12-06 14:43
 */
public final class SingletonFactory {

    private static Map<String, Object> objectMap = new HashMap<>();

    private SingletonFactory() {
    }

    public static <T> T getInstance(Class<T> c) {
        String key = c.toString();

        Object instance = objectMap.get(key);

        synchronized (c) {
            try {
                if (instance == null) {
                    instance = c.newInstance();
                    objectMap.put(key, instance);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return c.cast(instance);
    }

}
