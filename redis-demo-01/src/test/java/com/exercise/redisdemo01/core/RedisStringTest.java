package com.exercise.redisdemo01.core;

import com.exercise.redisdemo01.core.provider.StringCacheProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis字符串类型操作测试类
 * 用于测试StringCacheProvider接口的各种实现方法
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/15
 */
@Slf4j
@SpringBootTest
class RedisStringTest {

    @Resource
    private StringCacheProvider cacheProvider;

    /**
     * 测试基本的字符串设置和获取功能
     */
    @Test
    void testSetString() {
        String key = "test:setString:key";
        String value = "testValue";
        cacheProvider.setString(key, value);
        Assertions.assertEquals(value, cacheProvider.getString(key));
        cacheProvider.delete(key);
    }

    /**
     * 测试仅当key不存在时才设置字符串的功能
     */
    @Test
    void testSetStringWhenNotExists() {
        String key = "test:setStringWhenNotExists:key";
        String value = "testValue";
        Boolean result = cacheProvider.setStringWhenNotExists(key, value);
        Assertions.assertTrue(result);
        Assertions.assertEquals(value, cacheProvider.getString(key));
        
        // 再次设置应该失败
        Boolean result2 = cacheProvider.setStringWhenNotExists(key, "newValue");
        Assertions.assertFalse(result2);
        Assertions.assertEquals(value, cacheProvider.getString(key)); // 值不变
        cacheProvider.delete(key);
    }

    /**
     * 测试带过期时间的"仅当key不存在时设置"功能
     */
    @Test
    void testSetStringWhenNotExistsWithExpire() {
        String key = "test:setStringWhenNotExistsWithExpire:key";
        String value = "testValue";
        long expire = 10L;
        Boolean result = cacheProvider.setStringWhenNotExists(key, value, expire);
        Assertions.assertTrue(result);
        Assertions.assertEquals(value, cacheProvider.getString(key));
        Assertions.assertTrue(cacheProvider.ttlKey(key) > 0);
        
        // 再次设置应该失败
        Boolean result2 = cacheProvider.setStringWhenNotExists(key, "newValue", expire);
        Assertions.assertFalse(result2);
        Assertions.assertEquals(value, cacheProvider.getString(key)); // 值不变
        cacheProvider.delete(key);
    }

    /**
     * 测试获取并设置字符串的功能
     */
    @Test
    void testGetAndSetString() {
        String key = "test:getAndSetString:key";
        String oldValue = "oldValue";
        String newValue = "newValue";
        
        cacheProvider.setString(key, oldValue);
        String result = cacheProvider.getAndSetString(key, newValue);
        Assertions.assertEquals(oldValue, result);
        Assertions.assertEquals(newValue, cacheProvider.getString(key));
        cacheProvider.delete(key);
    }

    /**
     * 测试带过期时间的字符串设置功能
     */
    @Test
    void testSetStringWithExpire() {
        String key = "test:setStringWithExpire:key";
        String value = "testValue";
        long expire = 5L;
        cacheProvider.setString(key, value, expire);
        Assertions.assertEquals(value, cacheProvider.getString(key));
        Assertions.assertTrue(cacheProvider.ttlKey(key) > 0);
        cacheProvider.delete(key);
    }

    /**
     * 测试获取key的剩余生存时间功能
     */
    @Test
    void testTtlKey() {
        String key = "test:ttlKey:key";
        String value = "testValue";
        long expire = 10L;
        cacheProvider.setString(key, value, expire);
        long ttl = cacheProvider.ttlKey(key);
        Assertions.assertTrue(ttl > 0 && ttl <= expire);
        cacheProvider.delete(key);
        
        // 测试不存在的key
        long ttlNotExist = cacheProvider.ttlKey("notExistKey");
        Assertions.assertEquals(-2L, ttlNotExist); // Redis中不存在的key返回-2
    }

    /**
     * 测试获取字符串功能
     */
    @Test
    void testGetString() {
        String key = "test:getString:key";
        String value = "testValue";
        cacheProvider.setString(key, value);
        Assertions.assertEquals(value, cacheProvider.getString(key));
        cacheProvider.delete(key);
        
        // 测试不存在的key
        String notExistValue = cacheProvider.getString("notExistKey");
        Assertions.assertEquals("", notExistValue);
    }

    /**
     * 测试字符串自增功能
     */
    @Test
    void testIncrString() {
        String key = "test:incrString:key";
        cacheProvider.setString(key, "0");
        cacheProvider.incrString(key);
        Assertions.assertEquals("1", cacheProvider.getString(key));
        cacheProvider.incrString(key);
        Assertions.assertEquals("2", cacheProvider.getString(key));
        cacheProvider.delete(key);
    }

    /**
     * 测试字符串按指定数值自增功能
     */
    @Test
    void testIncrStringWithLongValue() {
        String key = "test:incrStringWithLongValue:key";
        cacheProvider.setString(key, "0");
        cacheProvider.incrString(key, 5L);
        Assertions.assertEquals("5", cacheProvider.getString(key));
        cacheProvider.incrString(key, 3L);
        Assertions.assertEquals("8", cacheProvider.getString(key));
        cacheProvider.delete(key);
    }

    /**
     * 测试字符串按指定浮点数值自增功能
     */
    @Test
    void testIncrStringWithDoubleValue() {
        String key = "test:incrStringWithDoubleValue:key";
        cacheProvider.setString(key, "0.5");
        cacheProvider.incrString(key, 1.5);
        double result = Double.parseDouble(cacheProvider.getString(key));
        Assertions.assertEquals(2.0, result, 0.001);
        cacheProvider.delete(key);
    }

    /**
     * 测试字符串自减功能
     */
    @Test
    void testDecrString() {
        String key = "test:decrString:key";
        cacheProvider.setString(key, "5");
        cacheProvider.decrString(key);
        Assertions.assertEquals("4", cacheProvider.getString(key));
        cacheProvider.decrString(key);
        Assertions.assertEquals("3", cacheProvider.getString(key));
        cacheProvider.delete(key);
    }

    /**
     * 测试字符串按指定数值自减功能
     */
    @Test
    void testDecrStringWithLongValue() {
        String key = "test:decrStringWithLongValue:key";
        cacheProvider.setString(key, "10");
        cacheProvider.decrString(key, 3L);
        Assertions.assertEquals("7", cacheProvider.getString(key));
        cacheProvider.decrString(key, 2L);
        Assertions.assertEquals("5", cacheProvider.getString(key));
        cacheProvider.delete(key);
    }

    /**
     * 测试使用Map批量设置字符串功能
     */
    @Test
    void testBatchSetStringWithMap() {
        Map<String, String> map = new HashMap<>();
        map.put("batchKey1", "batchValue1");
        map.put("batchKey2", "batchValue2");
        cacheProvider.batchSetString(map);
        
        Assertions.assertEquals("batchValue1", cacheProvider.getString("batchKey1"));
        Assertions.assertEquals("batchValue2", cacheProvider.getString("batchKey2"));
        
        cacheProvider.delete("batchKey1");
        cacheProvider.delete("batchKey2");
    }

    /**
     * 测试使用键值对数组批量设置字符串功能
     */
    @Test
    void testBatchSetStringWithArray() {
        cacheProvider.batchSetString("batchArrayKey1", "batchArrayValue1", "batchArrayKey2", "batchArrayValue2");
        
        Assertions.assertEquals("batchArrayValue1", cacheProvider.getString("batchArrayKey1"));
        Assertions.assertEquals("batchArrayValue2", cacheProvider.getString("batchArrayKey2"));
        
        cacheProvider.delete("batchArrayKey1");
        cacheProvider.delete("batchArrayKey2");
    }

    /**
     * 测试仅当key不存在时批量设置字符串功能
     */
    @Test
    void testBatchSetWhenNotExists() {
        Map<String, String> map = new HashMap<>();
        map.put("batchNotExistsKey1", "batchNotExistsValue1");
        map.put("batchNotExistsKey2", "batchNotExistsValue2");
        
        cacheProvider.batchSetWhenNotExists(map);
        Assertions.assertEquals("batchNotExistsValue1", cacheProvider.getString("batchNotExistsKey1"));
        Assertions.assertEquals("batchNotExistsValue2", cacheProvider.getString("batchNotExistsKey2"));
        
        // 再次设置相同的key应该不会覆盖
        Map<String, String> map2 = new HashMap<>();
        map2.put("newBatchKey", "newBatchValue");
        
        cacheProvider.batchSetWhenNotExists(map2);
        Assertions.assertEquals("batchNotExistsValue1", cacheProvider.getString("batchNotExistsKey1")); // 不变
        Assertions.assertEquals("newBatchValue", cacheProvider.getString("newBatchKey")); // 新增的应该设置成功
        
        cacheProvider.delete("batchNotExistsKey1");
        cacheProvider.delete("batchNotExistsKey2");
        cacheProvider.delete("newBatchKey");
    }

    /**
     * 测试设置字符串的指定位功能
     */
    @Test
    void testSetBitString() {
        String key = "test:setBitString:key";
        cacheProvider.setBitString(key, 0L, true);
        Boolean result = cacheProvider.getBitString(key, 0L);
        Assertions.assertTrue(result);
        cacheProvider.delete(key);
    }

    /**
     * 测试获取字符串的指定位功能
     */
    @Test
    void testGetBitString() {
        String key = "test:getBitString:key";
        cacheProvider.setBitString(key, 2L, true);
        Boolean result1 = cacheProvider.getBitString(key, 2L);
        Boolean result2 = cacheProvider.getBitString(key, 1L);
        Assertions.assertTrue(result1);
        Assertions.assertFalse(result2);
        cacheProvider.delete(key);
        
        // 测试不存在的key
        Boolean result3 = cacheProvider.getBitString("notExistBitKey", 0L);
        Assertions.assertFalse(result3);
    }

    /**
     * 测试删除字符串功能
     */
    @Test
    void testDelete() {
        String key = "test:delete:key";
        cacheProvider.setString(key, "testValue");
        Assertions.assertEquals("testValue", cacheProvider.getString(key));
        cacheProvider.delete(key);
        Assertions.assertEquals("", cacheProvider.getString(key));
    }
}
