package com.wzq.rpc.utils;

/**
 * @author wzq
 * @create 2022-12-12 19:17
 */
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

}
