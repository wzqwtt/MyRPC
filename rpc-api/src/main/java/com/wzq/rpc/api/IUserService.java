package com.wzq.rpc.api;

import com.wzq.rpc.pojo.User;

/**
 * 用户服务
 *
 * @author wzq
 * @create 2022-11-28 14:39
 */
public interface IUserService {

    /**
     * 根据ID查询用户
     *
     * @param id
     * @return
     */
    User getById(int id);

}
