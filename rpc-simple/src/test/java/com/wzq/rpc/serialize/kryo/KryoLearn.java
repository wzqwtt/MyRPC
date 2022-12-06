package com.wzq.rpc.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Kryo学习
 *
 * @author wzq
 * @create 2022-12-03 10:34
 */
@Slf4j
public class KryoLearn {
    
    public static class SomeClass {
        String value;
    }

    /**
     * Kryo的基本使用
     */
    @Test
    public void testKryo() {
        try {
            Kryo kryo = new Kryo();
            // 注册，在注册时，会为序列化类生成ID，这个ID是唯一的，标识该类型
            kryo.register(SomeClass.class);
            // 或者可以明确指定注册类的ID，但ID必须>0
            // kryo.register(SomeClass.class, 1);

            SomeClass object = new SomeClass();
            object.value = "Hello Kryo";

            // Kryo 类会自动执行序列化。Output 类和 Input 类负责处理缓冲字节，并写入到流中。

            // 序列化
            Output output = new Output(new FileOutputStream("file.bin"));
            kryo.writeObject(output, object);
            output.close();

            // 反序列化
            Input input = new Input(new FileInputStream("file.bin"));
            SomeClass someClass = kryo.readObject(input, SomeClass.class);
            input.close();

            log.info(someClass.value);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
