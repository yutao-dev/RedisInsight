package com.exercise.redisdemo01.core;

import com.exercise.redisdemo01.core.provider.CacheProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

/**
 * Redis连接信息测试类
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/10
 */
@Slf4j
@SpringBootTest
class RedisConnectionTest {

    @Resource
    private CacheProvider cacheProvider;

    @Test
    void testConnection() {
        Map<String, String> stringStringMap = cacheProvider.infoMemory();
        log.info("获取到内存信息: {}", stringStringMap);
    }

    @Test
    void testInfoMemory() {
        String monitorAlerts = cacheProvider.getMemoryMetrics().monitorAlerts();

        if (monitorAlerts.isEmpty()) {
            log.info("监控指标正常");
        } else {
            log.info(monitorAlerts);
        }
    }
}
