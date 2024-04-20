package com.lam.dating.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lam.dating.model.entity.User;
import com.lam.dating.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author AidenLam
 * @date 2024/4/21
 */
//启用组件注解开启定时任务
//@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Page<User>> redisTemplate;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);

    // 每天执行，预热推荐用户
    @Scheduled(cron = "0 12 1 * * *")
    public void doCacheRecommendUsers() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
        String redisKey = String.format("MeiQiu:user:recommend:%s", mainUserList);
        ValueOperations<String, Page<User>> operations = redisTemplate.opsForValue();
        try {
            operations.set(redisKey, userPage, 30000, TimeUnit.MICROSECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
    }

}
