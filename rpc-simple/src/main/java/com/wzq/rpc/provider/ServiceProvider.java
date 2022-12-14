package com.wzq.rpc.provider;

import com.wzq.rpc.entity.RpcServiceProperties;

/**
 * 保存和提供服务实例对象。服务端使用
 *
 * @author wzq
 * @create 2022-12-05 14:28
 */
public interface ServiceProvider {

    /**
     * 保存服务实例对象 和 服务实例对象实现的接口类的对应关系
     *
     * @param service              服务实例对象
     * @param serviceClass         服务实例对象实现的接口类
     * @param rpcServiceProperties 服务相关的属性
     */
    void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties);

    /**
     * 获取服务实例对象
     *
     * @param rpcServiceProperties 服务相关的属性
     * @return 服务实例对象
     */
    Object getService(RpcServiceProperties rpcServiceProperties);

    /**
     * 发布服务
     *
     * @param service              服务实例对象
     * @param rpcServiceProperties 服务相关的属性
     */
    void publishService(Object service, RpcServiceProperties rpcServiceProperties);

    /**
     * 发布服务
     *
     * @param service 服务实例对象
     */
    void publishService(Object service);
}
