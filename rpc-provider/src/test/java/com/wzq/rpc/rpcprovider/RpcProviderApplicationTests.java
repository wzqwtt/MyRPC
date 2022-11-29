package com.wzq.rpc.rpcprovider;

import com.alibaba.fastjson.JSON;
import com.wzq.rpc.common.RpcRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.UUID;

@SpringBootTest
class RpcProviderApplicationTests {

    @Test
    void contextLoads() {

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setClassName("com.wzq.rpc.api.IUserService");
        rpcRequest.setMethodName("getById");
        rpcRequest.setParameterTypes(new Class[]{int.class});
        rpcRequest.setParameters(new Object[]{1});

        String msg = JSON.toJSONString(rpcRequest);
        System.out.println(msg);

        RpcRequest rpcRequest1 = JSON.parseObject(msg, RpcRequest.class);
        System.out.println(rpcRequest1);

//        Person person = new Person(20, "John", "Doe", new Date());
//        String jsonObject = JSON.toJSONString(person);
//        System.out.println(jsonObject);
//        Person newPerson = JSON.parseObject(jsonObject, Person.class);

    }

    class Person {
        private int age;
        private String name;
        private String name2;
        private Date date;

        public Person(int age, String name, String name2, Date date) {
            this.age = age;
            this.name = name;
            this.name2 = name2;
            this.date = date;
        }
    }


}
