package com.wzq.rpc.rpcprovider.service;

import com.wzq.rpc.api.IUserService;
import com.wzq.rpc.pojo.User;
import com.wzq.rpc.rpcprovider.anno.RpcService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wzq
 * @create 2022-11-28 14:47
 */
@RpcService
@Service
public class UserServiceImpl implements IUserService {

    Map<Object, User> userMap = new HashMap<>();

    @Override
    public User getById(int id) {

        if (userMap.size() == 0) {
            User user1 = new User(1, "张三");
            userMap.put(user1.getId(), user1);

            User user2 = new User(2, "李四");
            userMap.put(user2.getId(), user2);
        }

        return userMap.get(id);
    }
}
