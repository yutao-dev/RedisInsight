package com.exercise.redisdemo01.core.provider.impl;

import com.exercise.redisdemo01.core.provider.DistributedLockProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁操作，基于Redisson底层实现类
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/9
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockRedissonProvider implements DistributedLockProvider {

    private final RedissonClient redissonClient;

    /**
     * 尝试获取分布式锁
     *
     * @param key      锁标识，用于唯一标识一个锁资源
     * @param waitTime 获取锁的最大等待时间，超过该时间则放弃获取锁
     * @param holdTime 锁的持有时间，即锁自动释放的时间
     * @return 是否成功获取锁，true表示成功，false表示失败
     * @throws InterruptedException 当线程在等待锁的过程中被中断时抛出
     */
    @Override
    public boolean tryLock(String key, long waitTime, long holdTime) throws InterruptedException {
        // 通过Redisson客户端获取指定key的可重入锁实例
        RLock lock = redissonClient.getLock(key);

        log.debug("尝试获取锁 key={}, holdTime={}ms", key, holdTime);
        boolean tryLock = false;
        try {
            // 尝试获取锁，最多等待waitTime毫秒，持有锁holdTime毫秒后自动释放
            tryLock = lock.tryLock(waitTime, holdTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // 恢复中断状态，以便调用栈上层能正确处理中断信号
            Thread.currentThread().interrupt();
            log.error("获取锁出现异常! key={}", key, e);
            // 包装为运行时异常抛出，避免强制调用方处理检查型异常
            throw new IllegalStateException("获取锁失败: " + e.getMessage(), e);
        }

        if (!tryLock) {
            log.warn("获取锁超时或失败！key={}", key);
        } else {
            log.debug("获取锁成功！key={}, holdTime={}ms", key, holdTime);
        }
        return tryLock;
    }

    /**
     * 安全地释放分布式锁
     * 只有当前持有锁的线程才能释放锁，防止误释放其他线程持有的锁
     *
     * @param key 锁标识，用于唯一标识要释放的锁资源
     */
    @Override
    public void unlock(String key) {
        log.debug("准备释放锁, key={}", key);
        // 获取与key关联的锁实例
        RLock lock = redissonClient.getLock(key);
        // 检查锁是否被任何线程持有，并且是否由当前线程持有
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("成功释放锁！key={}", key);
        } else {
            log.warn("未持有该锁或锁已释放，无需执行解锁操作。key={}", key);
        }
    }
}
