package com.exercise.redisdemo01.core.provider.impl;

import com.exercise.redisdemo01.core.provider.LockProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁提供者实现类
 * 
 * 基于Redis的SETNX命令实现分布式锁机制，提供获取锁和释放锁的功能。
 * 锁的过期时间可配置，防止死锁情况发生。
 * 
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LockRedisProvider implements LockProvider {
    
    /**
     * Redis操作模板类，用于执行Redis命令
     */
    private final StringRedisTemplate redisTemplate;
    
    /**
     * 尝试获取分布式锁
     * 
     * 使用Redis的SETNX命令（setIfAbsent）实现加锁操作，
     * 只有当key不存在时才能设置成功，从而保证锁的互斥性。
     * 
     * @param key 锁的键名，用于唯一标识一把锁
     * @param value 锁的值，通常为请求标识，用于验证锁的拥有者
     * @param expire 锁的过期时间（秒），防止死锁
     * @return true-获取锁成功，false-获取锁失败
     */
    @Override
    public boolean tryLock(String key, String value, long expire) {
        try {
            log.debug("尝试获取锁: key={}, value={}, expire={}s", key, value, expire);
            // 使用SETNX命令尝试设置键值对，如果key不存在则设置成功返回true
            Boolean tryLock = redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);

            // 判断是否成功获取锁
            boolean isLocked = Boolean.TRUE.equals(tryLock);
            if (isLocked) {
                log.debug("获取锁成功: key={}, value={}, expire={}s", key, value, expire);
            } else {
                log.debug("获取锁失败: key={}, value={}, expire={}s", key, value, expire);
            }
            return isLocked;
        } catch (Exception e) {
            log.error("获取锁失败: key={}, value={}, expire={}s", key, value, expire, e);
            return false;
        }
    }
    
    /**
     * 释放分布式锁
     * 
     * 在释放锁之前会验证当前锁的值是否与请求标识一致，
     * 防止误释放其他请求持有的锁。
     * 
     * @param key 锁的键名
     * @param value 锁的值，用于验证锁的拥有者
     * @return true-释放锁成功，false-释放锁失败
     */
    @Override
    public boolean releaseLock(String key, String value) {
        try {
            log.debug("尝试释放锁: key={}, value={}", key, value);
            // 获取当前锁的值
            String currentValue = redisTemplate.opsForValue().get(key);
            // 验证请求标识与锁的值是否一致
            if (value.equals(currentValue)) {
                log.debug("释放锁成功: key={}, value={}", key, value);
                // 删除键以释放锁
                redisTemplate.delete(key);
                return true;
            }
            log.debug("释放锁失败: key={}, value={}", key, value);
            return false;
        } catch (Exception e) {
            log.error("释放锁失败: key={}, value={}", key, value, e);
            return false;
        }
    }
}
