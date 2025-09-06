package com.exercise.redisdemo01.core.provider;

/**
 * 分布式锁操作接口提供，专注于高级功能提供
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/9
 */
public interface DistributedLockProvider {

    /**
     * 尝试获取锁
     * @param key 锁标识
     * @param waitTime 最大等待时间(ms)
     * @param holdTime 锁持有时间(ms)
     * @return 是否获锁成功
     */
    boolean tryLock(String key, long waitTime, long holdTime) throws InterruptedException;

    /**
     * 释放锁
     * @param key 锁标识
     */
    void unlock(String key);
}
