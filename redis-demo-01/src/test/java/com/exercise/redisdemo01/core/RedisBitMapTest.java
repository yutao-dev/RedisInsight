package com.exercise.redisdemo01.core;

import com.exercise.redisdemo01.core.provider.BitMapCacheProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/23
 */
@Slf4j
@SpringBootTest
class RedisBitMapTest {

    @Resource
    private BitMapCacheProvider bitMapCacheProvider;

    /**
     * 测试设置位图中指定偏移量的值
     */
    @Test
    public void testSetBit() {
        String key = "test:setBit:key";
        long offset = 100L;
        boolean value = true;

        bitMapCacheProvider.setBit(key, offset, value);
        Boolean result = bitMapCacheProvider.getBit(key, offset);
        Assertions.assertEquals(value, result);

        // 清理测试数据
        bitMapCacheProvider.delete(key);
    }

    /**
     * 测试获取位图中指定偏移量的值
     */
    @Test
    public void testGetBit() {
        String key = "test:getBit:key";
        long offset = 200L;
        boolean value = false;

        // 先设置一个值
        bitMapCacheProvider.setBit(key, offset, value);
        Boolean result = bitMapCacheProvider.getBit(key, offset);
        Assertions.assertEquals(value, result);

        // 测试获取一个未设置的位，应该返回false
        Boolean result2 = bitMapCacheProvider.getBit(key, offset + 1);
        Assertions.assertFalse(result2);

        // 清理测试数据
        bitMapCacheProvider.delete(key);
    }

    /**
     * 测试统计位图中被设置为1的位数
     */
    @Test
    public void testBitCount() {
        String key = "test:bitCount:key";
        
        // 设置几个位为1
        bitMapCacheProvider.setBit(key, 0L, true);
        bitMapCacheProvider.setBit(key, 1L, true);
        bitMapCacheProvider.setBit(key, 10L, true);
        bitMapCacheProvider.setBit(key, 100L, true);
        
        Long count = bitMapCacheProvider.bitCount(key);
        Assertions.assertEquals(4L, count);
        
        // 清理测试数据
        bitMapCacheProvider.delete(key);
    }

    /**
     * 测试查找位图中第一个被设置为指定值的位的位置
     */
    @Test
    public void testBitPos() {
        String key = "test:bitPos:key";
        
        // 设置一些位
        bitMapCacheProvider.setBit(key, 0L, false);
        bitMapCacheProvider.setBit(key, 1L, false);
        bitMapCacheProvider.setBit(key, 2L, true);
        bitMapCacheProvider.setBit(key, 3L, true);
        
        // 查找第一个为true的位
        Long posTrue = bitMapCacheProvider.bitPos(key, true);
        Assertions.assertEquals(2L, posTrue);
        
        // 查找第一个为false的位
        Long posFalse = bitMapCacheProvider.bitPos(key, false);
        Assertions.assertEquals(0L, posFalse);
        
        // 清理测试数据
        bitMapCacheProvider.delete(key);
    }

    /**
     * 测试对一个或多个位图执行按位操作，并将结果存储到目标位图中
     */
    @Test
    public void testBitOp() {
        String key1 = "test:bitOp:key1";
        String key2 = "test:bitOp:key2";
        String destKey = "test:bitOp:destKey";
        
        // 设置两个位图
        bitMapCacheProvider.setBit(key1, 0L, true);
        bitMapCacheProvider.setBit(key1, 1L, false);
        bitMapCacheProvider.setBit(key2, 0L, true);
        bitMapCacheProvider.setBit(key2, 1L, true);
        
        // 执行AND操作
        bitMapCacheProvider.bitOp("AND", destKey, key1, key2);
        Boolean result0 = bitMapCacheProvider.getBit(destKey, 0L);
        Boolean result1 = bitMapCacheProvider.getBit(destKey, 1L);
        Assertions.assertTrue(result0);
        Assertions.assertFalse(result1);
        
        // 清理测试数据
        bitMapCacheProvider.delete(key1);
        bitMapCacheProvider.delete(key2);
        bitMapCacheProvider.delete(destKey);
    }

    /**
     * 测试获取位图的内存使用量（字节）
     */
    @Test
    public void testMemoryUsage() {
        String key = "test:memoryUsage:key";
        
        // 设置一些位
        bitMapCacheProvider.setBit(key, 0L, true);
        bitMapCacheProvider.setBit(key, 1000L, true);
        
        Long memoryUsage = bitMapCacheProvider.memoryUsage(key);
        Assertions.assertNotNull(memoryUsage);
        Assertions.assertTrue(memoryUsage > 0);
        
        // 清理测试数据
        bitMapCacheProvider.delete(key);
    }

    /**
     * 测试删除指定的位图键
     */
    @Test
    public void testDelete() {
        String key = "test:delete:key";
        
        // 设置一些位
        bitMapCacheProvider.setBit(key, 0L, true);
        Boolean beforeDelete = bitMapCacheProvider.getBit(key, 0L);
        Assertions.assertTrue(beforeDelete);
        
        // 删除键
        bitMapCacheProvider.delete(key);
        
        // 删除后应该获取不到值（默认为false）
        Boolean afterDelete = bitMapCacheProvider.getBit(key, 0L);
        Assertions.assertFalse(afterDelete);
    }
}
