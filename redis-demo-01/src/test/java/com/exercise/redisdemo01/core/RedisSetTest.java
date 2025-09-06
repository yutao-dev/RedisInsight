package com.exercise.redisdemo01.core;

import com.exercise.redisdemo01.core.provider.SetCacheProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;


import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/23
 */
@Slf4j
@SpringBootTest
class RedisSetTest {

    @Resource
    private SetCacheProvider setCacheProvider;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testSetWithList() {
        String key = "test:set:list";
        List<String> values = Arrays.asList("value1", "value2", "value3");

        setCacheProvider.set(key, values);

        Set<String> result = setCacheProvider.queryAll(key);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(values));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testSetWithKeyValuePairs() {
        String key1 = "test:set:pair1";
        String value1 = "value1";
        String key2 = "test:set:pair2";
        String value2 = "value2";

        setCacheProvider.set(key1, value1, key2, value2);

        assertTrue(setCacheProvider.isExist(key1, value1));
        assertTrue(setCacheProvider.isExist(key2, value2));

        // 清理测试数据
        setCacheProvider.remove(key1, value1);
        setCacheProvider.remove(key2, value2);
    }

    @Test
    void testSetWithKeyValuePairsInvalidArgument() {
        assertThrows(IllegalArgumentException.class, () -> {
            setCacheProvider.set("key1", "value1", "key2"); // 奇数个参数
        });
    }

    @Test
    void testAtomicitySet() {
        String key1 = "test:set:atomic1";
        String value1 = "value1";
        String key2 = "test:set:atomic2";
        String value2 = "value2";

        setCacheProvider.atomicitySet(key1, value1, key2, value2);

        assertTrue(setCacheProvider.isExist(key1, value1));
        assertTrue(setCacheProvider.isExist(key2, value2));

        // 清理测试数据
        setCacheProvider.remove(key1, value1);
        setCacheProvider.remove(key2, value2);
    }

    @Test
    void testAtomicitySetInvalidArgument() {
        assertThrows(IllegalArgumentException.class, () -> {
            setCacheProvider.atomicitySet("key1", "value1", "key2"); // 奇数个参数
        });
    }

    @Test
    void testQueryAll() {
        String key = "test:set:query";
        List<String> values = Arrays.asList("a", "b", "c");
        setCacheProvider.set(key, values);

        Set<String> result = setCacheProvider.queryAll(key);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(values));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testIsExist() {
        String key = "test:set:exist";
        String value = "existValue";
        setCacheProvider.set(key, Collections.singletonList(value));

        assertTrue(setCacheProvider.isExist(key, value));
        assertFalse(setCacheProvider.isExist(key, "nonExistValue"));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testRandomPop() {
        String key = "test:set:pop";
        List<String> values = Arrays.asList("a", "b", "c");
        setCacheProvider.set(key, values);

        String popped = setCacheProvider.randomPop(key);
        assertNotNull(popped);
        assertTrue(values.contains(popped));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testInterSectionTwoKeys() {
        String key1 = "test:set:inter1";
        String key2 = "test:set:inter2";
        setCacheProvider.set(key1, Arrays.asList("a", "b", "c"));
        setCacheProvider.set(key2, Arrays.asList("b", "c", "d"));

        Set<String> intersection = setCacheProvider.interSection(key1, key2);
        assertNotNull(intersection);
        assertEquals(2, intersection.size());
        assertTrue(intersection.contains("b"));
        assertTrue(intersection.contains("c"));

        // 清理测试数据
        setCacheProvider.remove(key1, "b");
        setCacheProvider.remove(key2, "c");
    }

    @Test
    void testInterSectionMultipleKeys() {
        String key1 = "test:set:inter1";
        String key2 = "test:set:inter2";
        String key3 = "test:set:inter3";
        setCacheProvider.set(key1, Arrays.asList("a", "b", "c"));
        setCacheProvider.set(key2, Arrays.asList("b", "c", "d"));
        setCacheProvider.set(key3, Arrays.asList("c", "d", "e"));

        Set<String> intersection = setCacheProvider.interSection(Arrays.asList(key1, key2, key3));
        assertNotNull(intersection);
        assertEquals(1, intersection.size());
        assertTrue(intersection.contains("c"));

        // 清理测试数据
        setCacheProvider.remove(key1, "b");
        setCacheProvider.remove(key2, "c");
    }

    @Test
    void testUnionSectionMultipleKeys() {
        String key1 = "test:set:union1";
        String key2 = "test:set:union2";
        String key3 = "test:set:union3";
        setCacheProvider.set(key1, Arrays.asList("a", "b"));
        setCacheProvider.set(key2, Arrays.asList("b", "c"));
        setCacheProvider.set(key3, Arrays.asList("c", "d"));

        Set<String> union = setCacheProvider.unionSection(Arrays.asList(key1, key2, key3));
        assertNotNull(union);
        assertEquals(4, union.size());
        assertTrue(union.containsAll(Arrays.asList("a", "b", "c", "d")));

        // 清理测试数据
        stringRedisTemplate.delete(key1);
        stringRedisTemplate.delete(key2);
        stringRedisTemplate.delete(key3);
    }

    @Test
    void testUnionSectionTwoKeys() {
        String key1 = "test:set:union1";
        String key2 = "test:set:union2";
        setCacheProvider.set(key1, Arrays.asList("a", "b"));
        setCacheProvider.set(key2, Arrays.asList("b", "c"));

        Set<String> union = setCacheProvider.unionSection(key1, key2);
        assertNotNull(union);
        assertEquals(3, union.size());
        assertTrue(union.containsAll(Arrays.asList("a", "b", "c")));

        // 清理测试数据
        stringRedisTemplate.delete(key1);
        stringRedisTemplate.delete(key2);
    }

    @Test
    void testRemove() {
        String key = "test:set:remove";
        String value = "toBeRemoved";
        setCacheProvider.set(key, Collections.singletonList(value));

        assertTrue(setCacheProvider.isExist(key, value));

        setCacheProvider.remove(key, value);

        assertFalse(setCacheProvider.isExist(key, value));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testZAdd() {
        String key = "test:zset:add";
        String value = "zValue";
        double score = 1.5;

        setCacheProvider.zAdd(key, value, score);

        Double resultScore = setCacheProvider.zScore(key, value);
        assertNotNull(resultScore);
        assertEquals(score, resultScore);

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testZAtomicityAdd() {
        String key1 = "test:zset:atomic1";
        String value1 = "zValue1";
        double score1 = 1.0;
        String key2 = "test:zset:atomic2";
        String value2 = "zValue2";
        double score2 = 2.0;

        setCacheProvider.zAtomicityAdd(key1, value1, String.valueOf(score1), key2, value2, String.valueOf(score2));

        Double resultScore1 = setCacheProvider.zScore(key1, value1);
        Double resultScore2 = setCacheProvider.zScore(key2, value2);
        assertNotNull(resultScore1);
        assertNotNull(resultScore2);
        assertEquals(score1, resultScore1);
        assertEquals(score2, resultScore2);

        // 清理测试数据
        stringRedisTemplate.delete(key1);
        stringRedisTemplate.delete(key2);
    }

    @Test
    void testZAtomicityAddInvalidArgument() {
        assertThrows(IllegalArgumentException.class, () -> {
            setCacheProvider.zAtomicityAdd("key1", "value1", "1.0", "key2"); // 不是3的倍数
        });
    }

    @Test
    void testZAddBatch() {
        String key1 = "test:zset:batch1";
        String value1 = "zValue1";
        double score1 = 1.0;
        String key2 = "test:zset:batch2";
        String value2 = "zValue2";
        double score2 = 2.0;

        setCacheProvider.zAdd(key1, value1, String.valueOf(score1), key2, value2, String.valueOf(score2));

        Double resultScore1 = setCacheProvider.zScore(key1, value1);
        Double resultScore2 = setCacheProvider.zScore(key2, value2);
        assertNotNull(resultScore1);
        assertNotNull(resultScore2);
        assertEquals(score1, resultScore1);
        assertEquals(score2, resultScore2);

        // 清理测试数据
        stringRedisTemplate.delete(key1);
        stringRedisTemplate.delete(key2);
    }

    @Test
    void testZAddBatchInvalidArgument() {
        assertThrows(IllegalArgumentException.class, () -> {
            setCacheProvider.zAdd("key1", "value1", "1.0", "key2"); // 不是3的倍数
        });
    }

    @Test
    void testZRemove() {
        String key = "test:zset:remove";
        String value = "zValue";
        double score = 1.5;

        setCacheProvider.zAdd(key, value, score);
        assertNotNull(setCacheProvider.zScore(key, value));

        setCacheProvider.zRemove(key, value);
        assertNull(setCacheProvider.zScore(key, value));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testZRange() {
        String key = "test:zset:range";
        setCacheProvider.zAdd(key, "a", 1.0);
        setCacheProvider.zAdd(key, "b", 2.0);
        setCacheProvider.zAdd(key, "c", 3.0);

        Set<String> range = setCacheProvider.zRange(key, 0, 1);
        assertNotNull(range);
        assertEquals(2, range.size());
        assertTrue(range.contains("a"));
        assertTrue(range.contains("b"));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testZRevRange() {
        String key = "test:zset:revrange";
        setCacheProvider.zAdd(key, "a", 1.0);
        setCacheProvider.zAdd(key, "b", 2.0);
        setCacheProvider.zAdd(key, "c", 3.0);

        Set<String> range = setCacheProvider.zRevRange(key, 0, 1);
        assertNotNull(range);
        assertEquals(2, range.size());
        assertTrue(range.contains("c"));
        assertTrue(range.contains("b"));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testZScore() {
        String key = "test:zset:score";
        String value = "zValue";
        double score = 1.5;

        setCacheProvider.zAdd(key, value, score);

        Double resultScore = setCacheProvider.zScore(key, value);
        assertNotNull(resultScore);
        assertEquals(score, resultScore);

        // 测试不存在的值
        Double nonExistScore = setCacheProvider.zScore(key, "nonExist");
        assertNull(nonExistScore);

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testZRank() {
        String key = "test:zset:rank";
        setCacheProvider.zAdd(key, "a", 1.0);
        setCacheProvider.zAdd(key, "b", 2.0);
        setCacheProvider.zAdd(key, "c", 3.0);

        Long rank = setCacheProvider.zRank(key, "b");
        assertNotNull(rank);
        assertEquals(1L, rank.longValue());

        // 测试不存在的值
        Long nonExistRank = setCacheProvider.zRank(key, "nonExist");
        assertNull(nonExistRank);

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testZRevRank() {
        String key = "test:zset:revrank";
        setCacheProvider.zAdd(key, "a", 1.0);
        setCacheProvider.zAdd(key, "b", 2.0);
        setCacheProvider.zAdd(key, "c", 3.0);

        Long rank = setCacheProvider.zRevRank(key, "b");
        assertNotNull(rank);
        assertEquals(1L, rank.longValue()); // c(0), b(1), a(2)

        // 测试不存在的值
        Long nonExistRank = setCacheProvider.zRevRank(key, "nonExist");
        assertNull(nonExistRank);

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testZCard() {
        String key = "test:zset:card";
        setCacheProvider.zAdd(key, "a", 1.0);
        setCacheProvider.zAdd(key, "b", 2.0);
        setCacheProvider.zAdd(key, "c", 3.0);

        Long size = setCacheProvider.zCard(key);
        assertNotNull(size);
        assertEquals(3L, size.longValue());

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testZRangeByScore() {
        String key = "test:zset:rangebyscore";
        setCacheProvider.zAdd(key, "a", 1.0);
        setCacheProvider.zAdd(key, "b", 2.0);
        setCacheProvider.zAdd(key, "c", 3.0);

        Set<String> range = setCacheProvider.zRangeByScore(key, 1.5, 2.5);
        assertNotNull(range);
        assertEquals(1, range.size());
        assertTrue(range.contains("b"));

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }

    @Test
    void testZCount() {
        String key = "test:zset:count";
        setCacheProvider.zAdd(key, "a", 1.0);
        setCacheProvider.zAdd(key, "b", 2.0);
        setCacheProvider.zAdd(key, "c", 3.0);

        Long count = setCacheProvider.zCount(key, 1.5, 2.5);
        assertNotNull(count);
        assertEquals(1L, count.longValue());

        // 清理测试数据
        stringRedisTemplate.delete(key);
    }
}
