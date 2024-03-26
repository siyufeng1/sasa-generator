package com.siyufeng.web.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 缓存操作
 */
@Component
public class CacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    Cache<String, Object> localCache = Caffeine.newBuilder()
            .expireAfterWrite(100, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public void put(String key, Object value){
        localCache.put(key, value);
        redisTemplate.opsForValue().set(key, value, 100, TimeUnit.MINUTES);
    }

    public Object get(String key){
        //先从本地缓存中获取
        Object value = localCache.getIfPresent(key);
        if(value != null){
            return value;
        }
        //本地缓存未命中，则从分布式缓存中获取
        value = redisTemplate.opsForValue().get(key);
        if(value != null){
            //将获取到的值放入本地缓存
            localCache.put(key, value);
        }
        return value;
    }

    public void delete(String key){
        localCache.invalidate(key);
        redisTemplate.delete(key);
    }
}
