package com.wzq.rpc.spi;

import com.wzq.rpc.extension.ExtensionLoader;

/**
 * @author wzq
 * @create 2022-12-13 19:24
 */
public class SPITestMain {

    public static void main(String[] args) {
        SPIInterfaceDemo hello1 = ExtensionLoader.getExtensionLoader(SPIInterfaceDemo.class).getExtension("hello1");
        hello1.sayHello("hello1");
    }

}
