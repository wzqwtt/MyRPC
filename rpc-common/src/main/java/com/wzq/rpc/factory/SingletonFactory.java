package com.wzq.rpc.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取单例对象的工厂类
 *
 * @author wzq
 * @create 2022-12-06 14:43
 */
public final class SingletonFactory {

    private static final Map<String, Object> OBJECT_MAP = new HashMap<>();

    private SingletonFactory() {
    }

    public static <T> T getInstance(Class<T> c) {
        String key = c.toString();

        Object instance = OBJECT_MAP.get(key);

        synchronized (c) {
            try {
                if (instance == null) {
                    instance = c.newInstance();
                    OBJECT_MAP.put(key, instance);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return c.cast(instance);
    }

}
