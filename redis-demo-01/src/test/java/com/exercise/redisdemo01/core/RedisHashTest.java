package com.exercise.redisdemo01.core;

import com.exercise.redisdemo01.core.provider.HashCacheProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/23
 */
@Slf4j
@SpringBootTest
class RedisHashTest {

    @Resource
    private HashCacheProvider hashCacheProvider;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testSetAndGet() {
        String key = "test:hash:setAndGet";
        String field = "field1";
        String value = "value1";

        hashCacheProvider.set(key, field, value);
        String result = hashCacheProvider.get(key, field);

        assertEquals(value, result);

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testDel() {
        String key = "test:hash:del";
        String field1 = "field1";
        String field2 = "field2";
        String value1 = "value1";
        String value2 = "value2";

        hashCacheProvider.set(key, field1, value1);
        hashCacheProvider.set(key, field2, value2);

        Long deletedCount = hashCacheProvider.del(key, field1, field2);

        assertEquals(2L, deletedCount.longValue());
        assertFalse(hashCacheProvider.isExists(key, field1));
        assertFalse(hashCacheProvider.isExists(key, field2));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testIsExists() {
        String key = "test:hash:isExists";
        String field = "field1";
        String value = "value1";

        hashCacheProvider.set(key, field, value);

        assertTrue(hashCacheProvider.isExists(key, field));
        assertFalse(hashCacheProvider.isExists(key, "nonExistField"));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testGetLength() {
        String key = "test:hash:getLength";
        String field1 = "field1";
        String field2 = "field2";
        String value1 = "value1";
        String value2 = "value2";

        hashCacheProvider.set(key, field1, value1);
        hashCacheProvider.set(key, field2, value2);

        Long length = hashCacheProvider.getLength(key);

        assertEquals(2L, length.longValue());

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testSetMap() {
        String key = "test:hash:setMap";
        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", "value2");

        hashCacheProvider.set(key, map);

        String result1 = hashCacheProvider.get(key, "field1");
        String result2 = hashCacheProvider.get(key, "field2");

        assertEquals("value1", result1);
        assertEquals("value2", result2);

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testGetMultipleFields() {
        String key = "test:hash:getMultiple";
        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", "value2");
        map.put("field3", "value3");

        hashCacheProvider.set(key, map);

        List<String> values = hashCacheProvider.get(key, "field1", "field2", "nonExistField");

        assertEquals(3, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
        assertEquals("", values.get(2)); // non exist field returns empty string

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testGetAll() {
        String key = "test:hash:getAll";
        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", "value2");

        hashCacheProvider.set(key, map);

        Map<String, String> result = hashCacheProvider.getAll(key);

        assertEquals(2, result.size());
        assertEquals("value1", result.get("field1"));
        assertEquals("value2", result.get("field2"));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testScanAll() {
        String key = "test:hash:scanAll";
        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", "value2");
        map.put("field3", "value3");

        hashCacheProvider.set(key, map);

        Map<String, String> result = hashCacheProvider.scanAll(key, 2);

        assertEquals(3, result.size());
        assertEquals("value1", result.get("field1"));
        assertEquals("value2", result.get("field2"));
        assertEquals("value3", result.get("field3"));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testGetKeys() {
        String key = "test:hash:getKeys";
        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", "value2");

        hashCacheProvider.set(key, map);

        Set<String> keys = hashCacheProvider.getKeys(key);

        assertEquals(2, keys.size());
        assertTrue(keys.contains("field1"));
        assertTrue(keys.contains("field2"));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testGetValues() {
        String key = "test:hash:getValues";
        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", "value2");

        hashCacheProvider.set(key, map);

        List<String> values = hashCacheProvider.getValues(key);

        assertEquals(2, values.size());
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testIncrBy() {
        String key = "test:hash:incrBy";
        String field = "counter";

        Long result1 = hashCacheProvider.incrBy(key, field, 5);
        Long result2 = hashCacheProvider.incrBy(key, field, 3);

        assertEquals(5L, result1.longValue());
        assertEquals(8L, result2.longValue());

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
 public void testIncrByFloat() {
        String key = "test:hash:incrByFloat";
        String field = "floatCounter";

        Double result1 = hashCacheProvider.incrByFloat(key, field, 1.5);
        Double result2 = hashCacheProvider.incrByFloat(key, field, 2.3);

        assertEquals(1.5, result1, 0.001);
        assertEquals(3.8, result2, 0.001);

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testSetIfAbsent() {
        String key = "test:hash:setIfAbsent";
        String field = "field1";
        String value = "value1";

        boolean result1 = hashCacheProvider.setIfAbsent(key, field, value);
        boolean result2 = hashCacheProvider.setIfAbsent(key, field, "value2");

        assertTrue(result1);
        assertFalse(result2);
        assertEquals(value, hashCacheProvider.get(key, field));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    public void testQueryFieldLen() {
        String key = "test:hash:queryFieldLen";
        String field = "field1";
        String value = "hello world";

        hashCacheProvider.set(key, field, value);

        Long length = hashCacheProvider.queryFieldLen(key, field);

        assertEquals(11L, length.longValue());

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }
}
