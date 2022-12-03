package com.wzq.rpc.exception;

/**
 * 序列化异常
 *
 * @author wzq
 * @create 2022-12-02 19:15
 */
public class SerializeException extends RuntimeException {

    public SerializeException(String message) {
        super(message);
    }

}
