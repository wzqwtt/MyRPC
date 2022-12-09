package com.wzq.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
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
