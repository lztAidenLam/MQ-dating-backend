package com.lam.dating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lam.dating.model.entity.User;


/**
 * 用户表(User)表数据库访问层
 *
 * @author makejava
 * @since 2024-04-12 10:55:03
 */

public interface UserMapper extends BaseMapper<User> {
    Integer updateUserInfoById(User user);
}
