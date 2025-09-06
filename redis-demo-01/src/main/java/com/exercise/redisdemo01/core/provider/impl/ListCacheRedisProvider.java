package com.exercise.redisdemo01.core.provider.impl;

import com.exercise.redisdemo01.core.provider.ListCacheProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 基于Redis实现的List类型操作，提供对Redis List数据结构的常用操作封装
 * <p>
 * 该类为常用工具类型，可根据业务需求随时扩展新的方法
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/9
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListCacheRedisProvider implements ListCacheProvider {

    private final StringRedisTemplate redisTemplate;

    /**
     * 从列表左侧（头部）插入一个元素
     *
     * @param key   缓存key，不能为空
     * @param value 要插入的值，不能为空
     */
    @Override
    public void leftPush(String key, String value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            log.debug("左侧添加元素缓存 key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("左侧添加元素缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 从列表右侧（尾部）插入一个元素
     *
     * @param key   缓存key，不能为空
     * @param value 要插入的值，不能为空
     */
    @Override
    public void rightPush(String key, String value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            log.debug("右侧添加元素缓存 key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("右侧添加元素缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 从列表左侧（头部）弹出一个元素，如果列表为空则返回null
     *
     * @param key 缓存key，不能为空
     * @return 弹出的元素值，可能为null
     */
    @Override
    public String leftPop(String key) {
        try {
            String value = redisTemplate.opsForList().leftPop(key);
            log.debug("左侧弹出元素缓存 key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            log.error("左侧弹出元素缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 从列表右侧（尾部）弹出一个元素，如果列表为空则返回null
     *
     * @param key 缓存key，不能为空
     * @return 弹出的元素值，可能为null
     */
    @Override
    public String rightPop(String key) {
        try {
            String value = redisTemplate.opsForList().rightPop(key);
            log.debug("右侧弹出元素缓存 key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            log.error("右侧弹出元素缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 获取指定key对应列表中的所有元素
     *
     * @param key 缓存key，不能为空
     * @return 所有元素组成的列表，不会为null
     */
    @Override
    public List<String> getAll(String key) {
        try {
            List<String> stringList = redisTemplate.opsForList().range(key, 0, -1);
            stringList = Objects.isNull(stringList) ? Collections.emptyList() : stringList;

            checkSizeAndDebug(key, stringList);
            return stringList;
        } catch (Exception e) {
            log.error("获取所有元素缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 获取指定key对应列表中指定范围的元素
     *
     * @param key   缓存key，不能为空
     * @param start 开始索引（包含），从0开始
     * @param end   结束索引（包含），-1表示最后一个元素
     * @return 指定范围内的元素组成的列表，不会为null
     */
    @Override
    public List<String> getRange(String key, long start, long end) {
        try {
            List<String> stringList = redisTemplate.opsForList().range(key, start, end);
            stringList = Objects.isNull(stringList) ? Collections.emptyList() : stringList;

            checkSizeAndDebug(key, stringList);
            return stringList;
        } catch (Exception e) {
            log.error("获取范围元素缓存失败 key={}, start={}, end={}", key, start, end, e);
            throw e;
        }
    }

    /**
     * 内部辅助方法：检查列表大小并在合理范围内打印调试日志
     *
     * @param key        缓存key
     * @param stringList 元素列表
     */
    private static void checkSizeAndDebug(String key, List<String> stringList) {
        if (stringList.size() < 20) {
            log.debug("获取缓存 key={}, value={}", key, stringList);
        }
    }

    /**
     * 获取指定key对应列表中指定索引位置的元素
     *
     * @param key   缓存key，不能为空
     * @param index 索引位置，支持负数（-1表示最后一个元素）
     * @return 指定索引位置的元素值，如果不存在则返回空字符串
     */
    @Override
    public String getIndex(String key, long index) {
        try {
            String value = redisTemplate.opsForList().index(key, index);
            log.debug("获取缓存 key={}, index={}, value={}", key, index, value);
            return Objects.isNull(value) ? "" : value;
        } catch (Exception e) {
            log.error("获取索引元素缓存失败 key={}, index={}", key, index, e);
            throw e;
        }
    }
    
    /**
     * 获取指定key对应列表中的元素个数
     *
     * @param key 缓存key，不能为空
     * @return 列表元素个数，如果key不存在则返回0
     */
    @Override
    public Long getLength(String key) {
        try {
            Long size = redisTemplate.opsForList().size(key);
            log.debug("获取缓存 key={}, size={}", key, size);
            return Objects.isNull(size) ? 0L : size;
        } catch (Exception e) {
            log.error("获取列表长度缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 删除指定key对应的缓存
     *
     * @param key 缓存key，不能为空
     */
    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("删除缓存 key={}", key);
        } catch (Exception e) {
            log.error("删除缓存失败 key={}", key, e);
            throw e;
        }
    }
}
