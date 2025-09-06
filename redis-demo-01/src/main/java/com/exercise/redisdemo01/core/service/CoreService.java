package com.exercise.redisdemo01.core.service;

import com.exercise.redisdemo01.core.provider.DistributedLockProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 核心业务类，用来进行对应接口的测试
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/9
 */
@Slf4j
public class CoreService {

   @Resource
   private DistributedLockProvider lockProvider;

    /**
     * 测试分布式锁
     */
    public void testDistributedLock() {
        try {
            if (lockProvider.tryLock("test", 5000, 5000)) {
                // 业务代码
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁失败！");
        } finally {
            lockProvider.unlock("test");
        }
    }
}
