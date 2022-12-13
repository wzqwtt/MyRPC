package com.wzq.rpc.spi;

import com.wzq.rpc.extension.ExtensionLoader;
import org.junit.Test;

/**
 * @author wzq
 * @create 2022-12-13 19:24
 */
public class SPITestMain {

    @Test
    public void spiTest() {
        SPIInterfaceDemo hello1 = ExtensionLoader.getExtensionLoader(SPIInterfaceDemo.class).getExtension("hello1");
        hello1.sayHello("hello1");
    }

}
