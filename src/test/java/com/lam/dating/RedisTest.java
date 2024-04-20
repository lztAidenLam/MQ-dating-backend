package com.lam.dating;

import com.lam.dating.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author AidenLam
 * @date 2024/4/19
 */

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedis(){
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set("mqStr", "mq");
        valueOperations.set("mqInt", 1);
        valueOperations.set("mqDouble", 2.0);
        //增
        User user = new User();
        user.setId(1L);
        user.setUsername("meiQiu");
        valueOperations.set("mqObj", user);

        //查
        Object mqStr = valueOperations.get("mqStr");
        Assertions.assertEquals("mq", (String) mqStr);
        Object mqInt = valueOperations.get("mqInt");
        Assertions.assertEquals(1, (int) (Integer) mqInt);
        Object mqDouble = valueOperations.get("mqDouble");
        Assertions.assertEquals(2.0, (Double) mqDouble);
        Object mqObj = valueOperations.get("mqObj");
        System.out.println((User)mqObj);

        //改
//        valueOperations.set("mqStr", "danDan");

        //删
//        redisTemplate.delete("mqObj");
    }
}
