package com.exercise.redisdemo01.core.provider.impl;

import com.exercise.redisdemo01.core.provider.BitMapCacheProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BitMapCacheRedisProvider implements BitMapCacheProvider {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 设置位图中指定偏移量的值
     *
     * @param key    位图的键
     * @param offset 偏移量
     * @param value  要设置的值（0 或 1）
     */
    @Override
    public void setBit(String key, long offset, boolean value) {
        try {
            stringRedisTemplate.opsForValue().setBit(key, offset, value);
            log.debug("设置位图成功 key={}, offset={}, value={}", key, offset, value);
        } catch (Exception e) {
            log.error("设置位图失败 key={}, offset={}, value={}", key, offset, value, e);
            throw e;
        }
    }

    /**
     * 获取位图中指定偏移量的值
     *
     * @param key    位图的键
     * @param offset 偏移量
     * @return 指定偏移量的值
     */
    @Override
    public Boolean getBit(String key, long offset) {
        try {
            Boolean value = stringRedisTemplate.opsForValue().getBit(key, offset);
            log.debug("获取位图值成功 key={}, offset={}, value={}", key, offset, value);
            return value;
        } catch (Exception e) {
            log.error("获取位图值失败 key={}, offset={}", key, offset, e);
            throw e;
        }
    }

    /**
     * 统计位图中被设置为1的位数
     *
     * @param key 位图的键
     * @return 被设置为1的位数
     */
    @Override
    public Long bitCount(String key) {
        try {
            Long count = stringRedisTemplate.execute(connection -> connection.stringCommands().bitCount(key.getBytes()), true);
            log.debug("统计位图中1的个数成功 key={}, count={}", key, count);
            return count;
        } catch (Exception e) {
            log.error("统计位图中1的个数失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 查找位图中第一个被设置为指定值的位的位置
     *
     * @param key   位图的键
     * @param value 要查找的值（0 或 1）
     * @return 第一个被设置为指定值的位的位置，如果不存在则返回-1
     */
    @Override
    public Long bitPos(String key, boolean value) {
        try {
            Long pos = stringRedisTemplate.execute(connection -> connection.stringCommands().bitPos(key.getBytes(), value), true);
            log.debug("查找位图中第一个{}的位置成功 key={}, pos={}", value ? 1 : 0, key, pos);
            return pos;
        } catch (Exception e) {
            log.error("查找位图中第一个{}的位置失败 key={}", value ? 1 : 0, key, e);
            throw e;
        }
    }

    /**
     * 对一个或多个位图执行按位操作，并将结果存储到目标位图中
     *
     * @param operation 操作类型（AND, OR, XOR, NOT）
     * @param destKey   目标位图的键
     * @param keys      源位图的键列表
     */
    @Override
    public void bitOp(String operation, String destKey, String... keys) {
        try {
            byte[][] keyBytes = new byte[keys.length][];
            for (int i = 0; i < keys.length; i++) {
                keyBytes[i] = keys[i].getBytes();
            }

            stringRedisTemplate.execute((RedisCallback<Object>) connection ->
                switch (operation.toUpperCase()) {
                    case "AND" ->
                            connection.stringCommands().bitOp(RedisStringCommands.BitOperation.AND, destKey.getBytes(), keyBytes);
                    case "OR" ->
                            connection.stringCommands().bitOp(RedisStringCommands.BitOperation.OR, destKey.getBytes(), keyBytes);
                    case "XOR" ->
                            connection.stringCommands().bitOp(RedisStringCommands.BitOperation.XOR, destKey.getBytes(), keyBytes);
                    case "NOT" -> {
                        if (keys.length != 1) {
                            throw new IllegalArgumentException("NOT操作只能接受一个源键");
                        }
                        yield connection.stringCommands().bitOp(RedisStringCommands.BitOperation.NOT, destKey.getBytes(), keyBytes[0]);
                    }
                    default -> throw new IllegalArgumentException("不支持的操作类型: " + operation);

            }, true);

            log.debug("位图操作成功 operation={}, destKey={}, keys={}", operation, destKey, keys);
        } catch (Exception e) {
            log.error("位图操作失败 operation={}, destKey={}, keys={}", operation, destKey, keys, e);
            throw e;
        }
    }

    /**
     * 获取位图的内存使用量（字节）
     *
     * @param key 位图的键
     * @return 内存使用量（字节）
     */
    @Override
    public Long memoryUsage(String key) {
        try {
            byte[] serialize = stringRedisTemplate.getStringSerializer().serialize(key);
            Long memoryUsage = (Long) stringRedisTemplate.execute((RedisCallback<Object>) connection ->
                    connection.execute("MEMORY USAGE", serialize));

            log.debug("获取位图内存使用量成功 key={}, usage={} bytes", key, memoryUsage);
            return memoryUsage;
        } catch (Exception e) {
            log.error("获取位图内存使用量失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 删除指定的位图键
     *
     * @param key 位图的键
     */
    @Override
    public void delete(String key) {
        try {
            stringRedisTemplate.delete(key);
            log.debug("删除位图成功 key={}", key);
        } catch (Exception e) {
            log.error("删除位图失败 key={}", key, e);
            throw e;
        }
    }
}
