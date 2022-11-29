package com.wzq.rpc.rpcconsumer.anno;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 引用代理类
 *
 * @author wzq
 * @create 2022-11-29 20:09
 */
// 用于标注在属性上
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {
}
