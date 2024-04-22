package com.lam.dating.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lam.dating.model.entity.User;
import com.lam.dating.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;

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
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String, Page<User>> redisTemplate;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);

    // 每天执行，预热推荐用户
    @Scheduled(cron = "0 12 1 * * *")
    public void doCacheRecommendUsers() {
        RLock lock = redissonClient.getLock("MeiQiu:precache:doCache:lock");

        try {
            // 只有一个线程能获取锁
            if (lock.tryLock(0, 1L, TimeUnit.MICROSECONDS)) {
                System.out.println("getLock:" + Thread.currentThread().getId());
            }
            for (Long userId : mainUserList) {
                // 查数据库
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                String redisKey = String.format("MeiQiu:user:recommend:%s", mainUserList);
                ValueOperations<String, Page<User>> operations = redisTemplate.opsForValue();
                // 写缓存，30秒过期
                try {
                    operations.set(redisKey, userPage, 30000, TimeUnit.MICROSECONDS);
                } catch (Exception e) {
                    log.error("redis set key error", e);
                }
            }

        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
