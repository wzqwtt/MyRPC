package com.wzq.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * RpcService注解，直接在需要调用的接口实现类上添加该注解，可以实现服务的自动注册
 * @author wzq
 * @create 2022-12-09 18:43
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface RpcService {

    /**
     * Service的版本，默认的值是空
     */
    String version() default "";

    /**
     * Service Group，默认值是空
     */
    String group() default "";

}
