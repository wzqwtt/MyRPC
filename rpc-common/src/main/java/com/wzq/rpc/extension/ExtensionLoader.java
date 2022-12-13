package com.wzq.rpc.extension;

import com.wzq.rpc.utils.Holder;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Dubbo SPI
 *
 * @author wzq
 * @create 2022-12-13 13:53
 */
@Slf4j
public class ExtensionLoader<T> {

    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";

    /**
     * ExtensionLoader缓存<p>
     * {@code key: Class<?>}接口; {@code value: ExtensionLoader<?>}该接口对应的ExtensionLoader
     */
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    /**
     * Extension实例<p>
     * {@code key: Class<?>}表示Class；{@code Object}表示该对象
     */
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    /**
     * 本ExtensionLoader对应的接口的Class
     */
    private final Class<?> type;

    /**
     * 配置文件中书写的格式是{@code key-value}形式，其中：<p>
     * {@code key: name}表示在配置；{@code Holder<Object>}表示该类的持有对象
     */
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    /**
     *
     */
    private final Holder<Map<String, Class<?>>> cachedClassed = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     * 用于从缓存中获取与拓展类对应的ExtensionLoader
     *
     * @param type 接口的Class
     * @param <S>  泛型
     * @return 返回一个ExtensionLoader对象
     */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        // 判空
        if (type == null) {
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        // 判断是否为一个接口
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        // 判断该接口是否标注了SPI注解
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("Extension type must be annotated by @SPI.");
        }

        // 首先从缓存中获取，如果没有命中，则创建一个
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            // 将创建的ExtensionLoader放入缓存中
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }

        return extensionLoader;
    }

    /**
     * 获取拓展类对象
     *
     * @param name 配置类中配置的key
     * @return 返回该key对应的实例对象
     */
    public T getExtension(String name) {
        // 判断name参数是否为空
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }

        // 首先从缓存中获取Extension，如果没有命中，那么创建一个
        Holder<Object> holder = cachedInstances.get(name);

        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }

        // 如果没有该实例，那么创建一个单例的对象
        Object instance = holder.get();
        // 双重检查
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    // 创建拓展实例
                    instance = createExtension(name);
                    // 设置实例到holder中
                    holder.set(instance);
                }
            }
        }

        return (T) instance;
    }

    /**
     * 创建拓展实例
     *
     * @param name key
     * @return 返回一个实例
     */
    private T createExtension(String name) {
        // 从配置文件中加载所有的拓展类，可得到“配置项名称”到“配置类”的映射关系表
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }

        T instance = (T) EXTENSION_INSTANCES.get(clazz);

        if (instance == null) {
            try {
                // 通过反射创建实例
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new RuntimeException("Fail to create an instance of the extension class " + clazz);
            }
        }

        return instance;
    }

    /**
     * 获取所有的拓展类
     *
     * @return 返回“配置项名称”到“配置类”的映射关系表，即{@code Map<名称, 拓展类>}
     */
    private Map<String, Class<?>> getExtensionClasses() {
        // 从缓存中获取已加载的拓展类
        Map<String, Class<?>> classes = cachedClassed.get();

        // 双重检查
        if (classes == null) {
            synchronized (cachedClassed) {
                classes = cachedClassed.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    // 从拓展目录加载所有的拓展到classes这个map中
                    loadDirectory(classes, SERVICE_DIRECTORY);
                    cachedClassed.set(classes);
                }
            }
        }

        return classes;
    }

    /**
     * 加载指定文件夹的配置文件
     *
     * @param extensionClasses
     * @param dir
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir) {
        // filename = 文件夹路径 + type的全限定名
        String fileName = dir + type.getName();

        try {
            // 类似迭代器
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    // 加载资源
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 用于读取和解析配置文件，并通过反射加载类
     *
     * @param extensionClasses
     * @param classLoader
     * @param resourceUrl
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8));
        ) {
            String line;
            // 读取每一行
            while ((line = reader.readLine()) != null) {
                // 获取注释的索引
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // #后面的注释直接忽略掉
                    line = line.substring(0, ci);
                }
                line = line.trim();

                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        // 这里的SPI使用 key-value的形式，所以他们都不能为空
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
