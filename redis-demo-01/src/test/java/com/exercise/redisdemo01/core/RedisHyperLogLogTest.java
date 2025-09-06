package com.exercise.redisdemo01.core;

import com.exercise.redisdemo01.core.provider.HyperLogLogProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/28
 */
@Slf4j
@SpringBootTest
class RedisHyperLogLogTest {
    
    @Resource
    private HyperLogLogProvider hyperLogLogProvider;
    
    @Test
    void testSave() {
        String key = "test_hyperloglog_key";
        String value = "test_value";
        
        boolean result = hyperLogLogProvider.save(key, value);
        log.info("保存结果: {}", result);
        
        // 验证保存成功
        Assertions.assertTrue(result, "元素应该被成功添加");
    }
    
    @Test
    void testQuerySize() {
        String key = "test_hyperloglog_key_count";
        String value1 = "value1";
        String value2 = "value2";
        String value3 = "value1"; // 重复值
        
        // 添加元素
        hyperLogLogProvider.save(key, value1);
        hyperLogLogProvider.save(key, value2);
        hyperLogLogProvider.save(key, value3); // 重复值不会增加计数
        
        long size = hyperLogLogProvider.querySize(key);
        log.info("不重复元素估计数量: {}", size);
        
        // 验证数量(因为HyperLogLog有误差，这里只是简单验证大于0)
        org.junit.jupiter.api.Assertions.assertTrue(size > 0, "不重复元素数量应该大于0");
    }
}
