package com.wzq.rpc.rpcprovider.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于暴露服务接口
 *
 * @author wzq
 * @create 2022-11-28 16:18
 */
// 用于类上
@Target(ElementType.TYPE)
// 在运行时可以获得
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {
}
