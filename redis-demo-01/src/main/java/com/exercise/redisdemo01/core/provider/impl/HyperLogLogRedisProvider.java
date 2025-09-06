package com.exercise.redisdemo01.core.provider.impl;

import com.exercise.redisdemo01.core.provider.HyperLogLogProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Redis HyperLogLog 数据结构操作实现类
 * 
 * HyperLogLog 是一种概率数据结构，用于基数统计（即统计不重复元素的个数）。
 * 它的优点是内存占用固定且很小（通常12K左右），但存在一定的误差率（约0.81%）。
 * 
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HyperLogLogRedisProvider implements HyperLogLogProvider {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 向指定的 HyperLogLog 中添加元素
     * 
     * 使用 Redis 的 PFADD 命令实现，该命令将元素添加到 HyperLogLog 数据结构中。
     * 如果元素已经存在，则不会增加计数；如果添加了新元素，则返回 true。
     * 
     * @param key   HyperLogLog 的键名
     * @param value 要添加的元素值
     * @return 如果至少有一个新元素被添加则返回 true，否则返回 false
     */
    @Override
    public boolean save(String key, String value) {
        Boolean result = stringRedisTemplate.opsForValue().getOperations().execute((RedisCallback<Boolean>) connection -> {
            Object executeResult = connection.execute("PFADD", key.getBytes(), value.getBytes());
            // PFADD 命令返回 1 表示至少有一个元素被添加，0 表示所有元素都已存在
            // executeResult 通常是 Long 类型
            if (executeResult instanceof Long longResult) {
                return longResult > 0;
            }
            return false;
        });
        log.debug("保存 HyperLogLog 成功 key={}, value={}, result={}", key, value, result);
        return result != null && result;
    }

    /**
     * 获取指定 HyperLogLog 中不重复元素的估计数量
     * 
     * 使用 Redis 的 PFCOUNT 命令实现，该命令返回 HyperLogLog 中不重复元素的近似数量。
     * 注意：由于是概率算法，返回值可能存在约0.81%的误差。
     * 
     * @param key HyperLogLog 的键名
     * @return 不重复元素的估计数量，如果发生异常则返回 0
     */
    @Override
    public long querySize(String key) {
        try {
            Long result = stringRedisTemplate.opsForValue().getOperations().execute((RedisCallback<Long>) connection -> {
                Object executeResult = connection.execute("PFCOUNT", key.getBytes());
                // PFCOUNT 命令返回 HyperLogLog 的元素数量
                if (executeResult instanceof Long longResult) {
                    return longResult;
                }
                return 0L;
            });
            log.debug("查询 HyperLogLog 元素数量成功 key={}, result={}", key, result);
            return Objects.isNull(result) ? 0L : result;
        } catch (Exception e) {
            log.error("查询 HyperLogLog 元素数量异常! key={}", key, e);
            return 0L;
        }
    }
}
