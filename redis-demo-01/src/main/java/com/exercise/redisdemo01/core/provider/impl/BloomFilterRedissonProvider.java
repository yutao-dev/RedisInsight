package com.exercise.redisdemo01.core.provider.impl;

import com.exercise.redisdemo01.core.provider.BloomFilterProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Redisson布隆过滤器实现类
 * 提供基于Redisson的布隆过滤器操作，包括单个和批量的数据存储与查询功能
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterRedissonProvider implements BloomFilterProvider {
    
    private final RedissonClient redissonClient;
    
    /**
     * 向布隆过滤器中添加单个元素
     * 
     * @param key 布隆过滤器的键名
     * @param value 要添加的值
     * @return 添加成功返回true，失败返回false
     */
    @Override
    public boolean save(String key, String value) {
        try {
            log.debug("向布隆过滤器中存储数据: key={}, value={}", key, value);
            boolean isSaved = redissonClient.getBloomFilter(key).add(value);
            if (!isSaved) {
                log.error("保存数据失败! key={}, value={}", key, value);
            } else {
                log.debug("保存数据成功! key={}, value={}", key, value);
            }
            return isSaved;
        } catch (Exception e) {
            log.error("保存数据出现异常！key={}, value={}", key, value, e);
            return false;
        }
    }
    
    /**
     * 向布隆过滤器中批量添加元素
     * 
     * @param key 布隆过滤器的键名
     * @param values 要添加的值集合
     * @return 添加成功返回true，失败返回false
     */
    @Override
    public boolean save(String key, Collection<String> values) {
        try {
            log.debug("向布隆过滤器中批量存储数据: key={}, values={}", key, values);
            boolean isSaved = redissonClient.getBloomFilter(key).add(values);
            if (!isSaved) {
                log.error("批量保存数据失败! key={}, values={}", key, values);
            } else {
                log.debug("批量保存数据成功! key={}, values={}", key, values);
            }
            return isSaved;
        } catch (Exception e) {
            log.error("批量保存数据出现异常！key={}, values={}", key, values, e);
            return false;
        }
    }
    
    /**
     * 判断布隆过滤器中是否存在指定元素
     * 
     * @param key 布隆过滤器的键名
     * @param value 要查询的值
     * @return 存在返回true，不存在返回false
     */
    @Override
    public boolean contains(String key, String value) {
        try {
            log.debug("从布隆过滤器中查询数据: key={}, value={}", key, value);
            boolean isExist = redissonClient.getBloomFilter(key).contains(value);
            if (!isExist) {
                log.error("查询数据失败! key={}, value={}", key, value);
            } else {
                log.debug("查询数据成功! key={}, value={}", key, value);
            }
            return isExist;
        } catch (Exception e) {
            log.error("查询数据出现异常！key={}, value={}", key, value, e);
            return false; 
        }
    }
    
    /**
     * 判断布隆过滤器中是否存在指定的多个元素
     * 
     * @param key 布隆过滤器的键名
     * @param values 要查询的值集合
     * @return 所有元素都存在返回true，否则返回false
     */
    @Override
    public boolean contains(String key, Collection<String> values) {
        try {
            log.debug("从布隆过滤器中批量查询数据: key={}, values={}", key, values);
            boolean isExist = redissonClient.getBloomFilter(key).contains(values);
            if (!isExist) {
                log.error("批量查询数据失败! key={}, values={}", key, values);
            } else {
                log.debug("批量查询数据成功! key={}, values={}", key, values);
            }
            return isExist;
        } catch (Exception e) {
            log.error("批量查询数据出现异常！key={}, values={}", key, values, e);
            return false;
        }
    }

    /**
     * 查询布隆过滤器中元素数量
     *
     * @param key 布隆过滤器的键名
     * @return 元素数量
     */
    public long querySize(String key) {
        try {
            log.debug("查询布隆过滤器中元素数量: key={}", key);
            long count = redissonClient.getBloomFilter(key).count();
            log.debug("查询布隆过滤器中元素数量成功: key={}, count={}", key, count);
            return count;
        } catch (Exception e) {
            log.error("查询布隆过滤器中元素数量异常！: key={}", key, e);
            return -1;
        }
    }
}
