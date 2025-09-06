package com.exercise.redisdemo01.core.provider;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/28
 */
public interface LockProvider {

    /**
     * 尝试获取分布式锁
     *
     * @param key 锁的键名，用于唯一标识一把锁
     * @param value 锁的值，通常为请求标识，用于验证锁的拥有者
     * @param expire 锁的过期时间（秒），防止死锁
     * @return true-获取锁成功，false-获取锁失败
     */
    boolean tryLock(String key, String value, long expire);

    /**
     * 释放分布式锁
     *
     * @param key 锁的键名
     * @param value 锁的值，用于验证锁的拥有者
     * @return true-释放锁成功，false-释放锁失败
     */
    boolean releaseLock(String key, String value);
}
