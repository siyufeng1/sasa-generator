package com.siyufeng.web;

import com.siyufeng.web.model.entity.Generator;
import com.siyufeng.web.service.GeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

/**
 * 主类测试
 *

 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private GeneratorService generatorService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testInsert(){
        stringRedisTemplate.opsForValue().set("test", "1");
        System.out.println(stringRedisTemplate.opsForValue().get("test"));
    }
}
