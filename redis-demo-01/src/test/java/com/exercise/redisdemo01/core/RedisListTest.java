package com.exercise.redisdemo01.core;

import com.exercise.redisdemo01.core.provider.ListCacheProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Redis列表类型操作测试类
 * 用于测试ListCacheProvider接口的各种实现方法
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/15
 */
@Slf4j
@SpringBootTest
class RedisListTest {

    @Resource
    private ListCacheProvider cacheProvider;

    /**
     * 测试从列表左侧插入元素的功能
     */
    @Test
    void testLeftPush() {
        String key = "test:leftPush:key";
        String value = "testValue";
        cacheProvider.leftPush(key, value);
        List<String> result = cacheProvider.getAll(key);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(value, result.get(0));
        cacheProvider.delete(key);
    }

    /**
     * 测试从列表右侧插入元素的功能
     */
    @Test
    void testRightPush() {
        String key = "test:rightPush:key";
        String value1 = "testValue1";
        String value2 = "testValue2";
        cacheProvider.rightPush(key, value1);
        cacheProvider.rightPush(key, value2);
        List<String> result = cacheProvider.getAll(key);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(value1, result.get(0));
        Assertions.assertEquals(value2, result.get(1));
        cacheProvider.delete(key);
    }

    /**
     * 测试从列表左侧弹出元素的功能
     */
    @Test
    void testLeftPop() {
        String key = "test:leftPop:key";
        String value1 = "testValue1";
        String value2 = "testValue2";
        cacheProvider.rightPush(key, value1);
        cacheProvider.rightPush(key, value2);
        
        String result1 = cacheProvider.leftPop(key);
        Assertions.assertEquals(value1, result1);
        
        String result2 = cacheProvider.leftPop(key);
        Assertions.assertEquals(value2, result2);
        
        String result3 = cacheProvider.leftPop(key);
        Assertions.assertNull(result3);
        
        cacheProvider.delete(key);
    }

    /**
     * 测试从列表右侧弹出元素的功能
     */
    @Test
    void testRightPop() {
        String key = "test:rightPop:key";
        String value1 = "testValue1";
        String value2 = "testValue2";
        cacheProvider.rightPush(key, value1);
        cacheProvider.rightPush(key, value2);
        
        String result1 = cacheProvider.rightPop(key);
        Assertions.assertEquals(value2, result1);
        
        String result2 = cacheProvider.rightPop(key);
        Assertions.assertEquals(value1, result2);
        
        String result3 = cacheProvider.rightPop(key);
        Assertions.assertNull(result3);
        
        cacheProvider.delete(key);
    }

    /**
     * 测试获取列表中所有元素的功能
     */
    @Test
    void testGetAll() {
        String key = "test:getAll:key";
        String value1 = "testValue1";
        String value2 = "testValue2";
        cacheProvider.rightPush(key, value1);
        cacheProvider.rightPush(key, value2);
        
        List<String> result = cacheProvider.getAll(key);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(value1, result.get(0));
        Assertions.assertEquals(value2, result.get(1));
        
        cacheProvider.delete(key);
        
        // 测试空列表
        List<String> emptyResult = cacheProvider.getAll("notExistKey");
        Assertions.assertEquals(0, emptyResult.size());
    }

    /**
     * 测试获取列表指定范围元素的功能
     */
    @Test
    void testGetRange() {
        String key = "test:getRange:key";
        for (int i = 1; i <= 5; i++) {
            cacheProvider.rightPush(key, "value" + i);
        }
        
        List<String> result = cacheProvider.getRange(key, 1, 3);
        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals("value2", result.get(0));
        Assertions.assertEquals("value3", result.get(1));
        Assertions.assertEquals("value4", result.get(2));
        
        cacheProvider.delete(key);
    }

    /**
     * 测试获取列表指定索引元素的功能
     */
    @Test
    void testGetIndex() {
        String key = "test:getIndex:key";
        String value1 = "testValue1";
        String value2 = "testValue2";
        cacheProvider.rightPush(key, value1);
        cacheProvider.rightPush(key, value2);
        
        String result1 = cacheProvider.getIndex(key, 0);
        Assertions.assertEquals(value1, result1);
        
        String result2 = cacheProvider.getIndex(key, -1);
        Assertions.assertEquals(value2, result2);
        
        String result3 = cacheProvider.getIndex(key, 10);
        Assertions.assertEquals("", result3);
        
        cacheProvider.delete(key);
    }

    /**
     * 测试获取列表长度的功能
     */
    @Test
    void testGetLength() {
        String key = "test:getLength:key";
        cacheProvider.rightPush(key, "value1");
        cacheProvider.rightPush(key, "value2");
        cacheProvider.rightPush(key, "value3");
        
        Long length = cacheProvider.getLength(key);
        Assertions.assertEquals(3L, length);
        
        cacheProvider.delete(key);
        
        // 测试不存在的key
        Long notExistLength = cacheProvider.getLength("notExistKey");
        Assertions.assertEquals(0L, notExistLength);
    }

    /**
     * 测试删除列表的功能
     */
    @Test
    void testDelete() {
        String key = "test:delete:key";
        cacheProvider.rightPush(key, "testValue");
        Assertions.assertEquals(1, cacheProvider.getLength(key));
        
        cacheProvider.delete(key);
        Assertions.assertEquals(0, cacheProvider.getLength(key));
    }
}
