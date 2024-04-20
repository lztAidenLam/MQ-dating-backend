package com.lam.dating.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lam.dating.common.ErrorCode;
import com.lam.dating.exception.BusinessException;
import com.lam.dating.mapper.UserMapper;
import com.lam.dating.model.entity.User;
import com.lam.dating.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lam.dating.constant.UserConstant.ADMIN_ROLE;

/**
 * 用户表(User)表服务实现类
 *
 * @author makejava
 * @since 2024-04-12 10:55:02
 */
@Service("userService")
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String SALT = "checkOne";

    private static final String USER_LOGIN_STATUS = "userLoginStatus";

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Page<User>> redisTemplate;

    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 校验是否符合传入参数是否符合要求
        // 密码加密
        // 向数据库插入数据
        /*
        1. 非空
        2. 账户长度不小于4位
        3. 密码长度不小于8位
        4. 账户不能重复
        5. 账户不包含特殊字符
        6. 密码和校验秘密相同
         */
        if (StrUtil.isAllBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        checkLegality(userAccount, userPassword);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 密码加密
        String verifyPassword = DigestUtils.md5DigestAsHex((SALT + userPassword)
                .getBytes(StandardCharsets.UTF_8));

        // 存入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(verifyPassword);
        this.save(user);
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 检验用户账号和密码是否合法
        // 跟数据库校验账号密码是否输入正确
        // 用户信息脱敏，返回用户信息
        /*
        1. 非空
        2. 账户长度不小于4位
        3. 密码长度不小于8位
        4. 账户不包含特殊字符
         */
        if (StrUtil.isAllBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        checkLegality(userAccount, userPassword);

        // 跟数据库校验账号密码是否输入正确
        String encoderPassword = DigestUtils.md5DigestAsHex((SALT + userPassword)
                .getBytes(StandardCharsets.UTF_8));
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        queryWrapper.eq(User::getPassword, encoderPassword);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount or userPassword is wrong!");
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 用户脱敏，返回用户信息
        User safetyUser = getSafetyUser(user);

        // 记录用户的登录态（session),将其存在服务器上
        request.getSession().setAttribute(USER_LOGIN_STATUS, safetyUser); //todo
        return safetyUser;
    }

    private void checkLegality(String userAccount, String userPassword) {
        if (userAccount.length() <= 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不小于3个字符");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不小于8个字符");
        }
        String validRule = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%…… &*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validRule).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊符号");
        }
    }

    @Override
    public List<User> selectAll() {
        List<User> userList = this.list();
//        ArrayList<User> userListVo = new ArrayList<>();
//        for (User user : userList) {
//            userListVo.add(getSafetyUser(user));
//        }

        return userList.stream().map(user -> getSafetyUser(user)).collect(Collectors.toList());
    }

    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无此用户");
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setIsDelete(originUser.getIsDelete());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public Integer userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATUS);
        return 1;
    }

    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//        for (String tag : tagNameList) {
//            queryWrapper.like(User::getTags, tag);
//        }
//        List<User> userList = userMapper.selectList(queryWrapper);
//        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());

        // 查询所有用户
        List<User> userList = this.selectAll();
        Gson gson = new Gson();
        // 判断内存中是否有符合要求的标签
        return userList.stream().filter(user -> {
            String tagStr = user.getTags();
            if (tagStr == null) {
                return false;
            }
            Set<String> tempTagNameList = gson.fromJson(tagStr, new TypeToken<Set<String>>() {
            }.getType());
            for (String tagName : tagNameList) {
                if (!tempTagNameList.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public Integer updateUser(User loginUser, User user) {
        Long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员，允许更新任何信息
        // 如果是普通用户，仅允许更新自己的信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User userOld = userMapper.selectById(userId);
        if (userOld == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        return userMapper.updateUserInfoById(user);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        return ADMIN_ROLE.equals(getLoginUser(request).getUserRole());
    }
    @Override
    public boolean isAdmin(User loginUser) {
        return ADMIN_ROLE.equals(loginUser.getUserRole());
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null) {
            return null;
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATUS);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return loginUser;
    }

    @Override
    public Page<User> getRecommendUsers(long pageNum, long pageSize, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        String redisKey = String.format("MeiQiu:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Page<User>> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存直接读取
        Page<User> userPage = valueOperations.get(redisKey);
        if (userPage != null) {
            return userPage;
        }
        // 如果没缓存，从数据库中查数据，并存入缓存中
        userPage = this.page(new Page<>(pageNum, pageSize), null);
        try {
            valueOperations.set(redisKey, userPage, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return userPage;
    }
}

