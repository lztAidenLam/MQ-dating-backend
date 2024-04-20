package com.lam.dating;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lam.dating.common.ErrorCode;
import com.lam.dating.exception.BusinessException;
import com.lam.dating.mapper.UserMapper;
import com.lam.dating.model.entity.User;
import com.lam.dating.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author AidenLam
 * @date 2024/4/12
 */

@SpringBootTest
@Slf4j
public class MainApplicationTest {

    @Autowired
    private UserMapper userMapper;

    @Resource
    private UserService userService;


    @Test
    public void testUsersPage() {
        Page<User> userPage = new Page<>(1, 2);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        Page<User> userList = userService.page(userPage);
        System.out.println(userList.getSize());
        userList.getRecords().forEach(System.out::println);
    }

    @Test
    public void testSelect() {
        User user = new User();
        user.setId(2L);
        user.setUserAccount("aiden");
        user.setEmail("lztAidenLam@gmail.com");
        Integer result = userMapper.updateUserInfoById(user);
        System.out.println(result);
    }

    @Test
    public void testLogicDelete() {
        boolean b = userService.removeById(1);
        System.out.println(b);
    }

    @Test
    public void testSelectUsersByTagsByJSON() {
        long starTime = System.currentTimeMillis();
        List<String> tagNameList = Arrays.asList("java", "golang");
        // 查询所有用户
        List<User> userList = userMapper.selectList(null);
        Gson gson = new Gson();
        // 判断内存中是否有符合要求的标签
        List<User> safetyUserList = userList.stream().filter(user -> {
            String tagStr = user.getTags();
            if (tagStr == null) {
                return false;
            }
            Set<String> tempTagNameList = gson.fromJson(tagStr, new TypeToken<Set<String>>() {
            }.getType());
            for (String tagName : tagNameList) {
                if (!tempTagNameList.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());

        safetyUserList.forEach(System.out::println);
        log.info("sql query time = " + (System.currentTimeMillis() - starTime));
    }

    @Test
    public void testSelectUsersByTags() {
        long starTime = System.currentTimeMillis();
        List<String> tagNameList = Arrays.asList("java", "golang");
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        for (String tag : tagNameList) {
            queryWrapper.like(User::getTags, tag);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        List<User> safetyUserList = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        safetyUserList.forEach(System.out::println);
        System.out.println(safetyUserList.size());
        log.info("sql query time = " + (System.currentTimeMillis() - starTime));
    }


    @Test
    public void testMBP() {
        List<User> userList = userMapper.selectList(null);
        userList.forEach(System.out::println);
        System.out.println("=================-------==================");
        System.out.println(userList.size());
    }

    @Test
    public void testUserRegister() {
        Long result = userService.userRegister("aidenlam", "123446689", "123446689");
        System.out.println(result);

//        User user = userService.getById(result);
//        if (user == null) {
//            System.out.println("无此用户");
//        }
//        System.out.println(user.getUserAccount());

    }

}
