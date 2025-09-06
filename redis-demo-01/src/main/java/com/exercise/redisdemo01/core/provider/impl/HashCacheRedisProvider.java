package com.exercise.redisdemo01.core.provider.impl;

import com.exercise.redisdemo01.core.provider.HashCacheProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HashCacheRedisProvider implements HashCacheProvider {

    private final StringRedisTemplate redisTemplate;

    /**
     * 设置哈希表中指定字段的值
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @param value 字段值
     */
    @Override
    public void set(String key, String field, String value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            log.debug("设置哈希字段成功 key={}, field={}, value={}", key, field, value);
        } catch (Exception e) {
            log.error("设置哈希字段失败 key={}, field={}, value={}", key, field, value, e);
            throw e;
        }
    }

    /**
     * 获取哈希表中指定字段的值
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @return 字段值，如果不存在则返回空字符串
     */
    @Override
    public String get(String key, String field) {
        try {
            Object value = redisTemplate.opsForHash().get(key, field);
            String result = value == null ? "" : value.toString();
            log.debug("获取哈希字段成功 key={}, field={}, value={}", key, field, result);
            return result;
        } catch (Exception e) {
            log.error("获取哈希字段失败 key={}, field={}", key, field, e);
            throw e;
        }
    }

    /**
     * 删除哈希表中一个或多个字段
     *
     * @param key    哈希表的键
     * @param fields 要删除的字段名数组
     * @return 被成功删除的字段数量
     */
    @Override
    public Long del(String key, String... fields) {
        try {
            Long result = redisTemplate.opsForHash().delete(key, (Object[]) fields);
            log.debug("删除哈希字段成功 key={}, fields={}, deletedCount={}", key, fields, result);
            return result;
        } catch (Exception e) {
            log.error("删除哈希字段失败 key={}, fields={}", key, fields, e);
            throw e;
        }
    }

    /**
     * 检查哈希表中指定字段是否存在
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @return 如果字段存在返回true，否则返回false
     */
    @Override
    public Boolean isExists(String key, String field) {
        try {
            Boolean exists = redisTemplate.opsForHash().hasKey(key, field);
            log.debug("检查哈希字段存在性 key={}, field={}, exists={}", key, field, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查哈希字段存在性失败 key={}, field={}", key, field, e);
            throw e;
        }
    }

    /**
     * 获取哈希表中字段的数量
     *
     * @param key 哈希表的键
     * @return 字段数量
     */
    @Override
    public Long getLength(String key) {
        try {
            Long size = redisTemplate.opsForHash().size(key);
            log.debug("获取哈希表字段数量 key={}, size={}", key, size);
            return size;
        } catch (Exception e) {
            log.error("获取哈希表字段数量失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 批量设置哈希表中的多个字段
     *
     * @param key 哈希表的键
     * @param map 包含字段和值的映射
     */
    @Override
    public void set(String key, Map<String, String> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            log.debug("批量设置哈希字段成功 key={}, map={}", key, map);
        } catch (Exception e) {
            log.error("批量设置哈希字段失败 key={}, map={}", key, map, e);
            throw e;
        }
    }

    /**
     * 批量获取哈希表中多个字段的值
     *
     * @param key    哈希表的键
     * @param fields 字段名数组
     * @return 字段值列表
     */
    @Override
    public List<String> get(String key, String... fields) {
        try {
            List<Object> values = redisTemplate.opsForHash().multiGet(key, java.util.Arrays.asList(fields));
            List<String> result = new java.util.ArrayList<>();
            for (Object value : values) {
                result.add(value == null ? "" : value.toString());
            }
            log.debug("批量获取哈希字段成功 key={}, fields={}, values={}", key, fields, result);
            return result;
        } catch (Exception e) {
            log.error("批量获取哈希字段失败 key={}, fields={}", key, fields, e);
            throw e;
        }
    }

    /**
     * 获取哈希表中所有的字段和值 (警惕大Key风险)
     *
     * @param key 哈希表的键
     * @return 包含所有字段和值的映射
     */
    @Override
    public Map<String, String> getAll(String key) {
        try {
            java.util.Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            java.util.Map<String, String> result = new java.util.HashMap<>();
            for (java.util.Map.Entry<Object, Object> entry : entries.entrySet()) {
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
            log.debug("获取哈希表所有字段成功 key={}, entries={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("获取哈希表所有字段失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 通过扫描的方式分批获取哈希表中所有的字段和值，避免一次性加载大量数据导致内存问题
     *
     * @param key       哈希表的键
     * @param batchSize 每次扫描的批次大小
     * @return 包含所有字段和值的映射
     */
    @Override
    public Map<String, String> scanAll(String key, int batchSize) {
        // 创建一个用于存储结果的Map
        Map<String, String> result = new HashMap<>();
        try (
                // 使用Redis的scan命令创建一个游标，用于分批扫描哈希表中的字段
                Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash()
                        .scan(key, ScanOptions.scanOptions().count(batchSize).build())
        ) {
            // 遍历游标中的每一个条目
            while (cursor.hasNext()) {
                // 获取下一个条目
                Map.Entry<Object, Object> entry = cursor.next();
                // 将条目的键和值转换为字符串并存入结果Map中
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
        } catch (Exception e) {
            // 记录错误日志
            log.error("扫描哈希表字段失败 key={}, batchSize={}", key, batchSize, e);
            // 抛出运行时异常
            throw e;
        }
        // 返回扫描结果
        return result;
    }

    /**
     * 获取哈希表中所有的字段名 (警惕大Key风险)
     *
     * @param key 哈希表的键
     * @return 字段名集合
     */
    @Override
    public Set<String> getKeys(String key) {
        try {
            Set<Object> keys = redisTemplate.opsForHash().keys(key);
            Set<String> result = new HashSet<>();
            for (Object k : keys) {
                String keyString = (String) k;
                result.add(keyString);
            }
            log.debug("获取哈希表所有字段名成功 key={}, keys={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("获取哈希表所有字段名失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 获取哈希表中所有的字段值 (警惕大Key风险)
     *
     * @param key 哈希表的键
     * @return 字段值集合
     */
    @Override
    public List<String> getValues(String key) {
        try {
            Collection<Object> values = redisTemplate.opsForHash().values(key);
            List<String> result = new ArrayList<>();
            for (Object value : values) {
                result.add(value == null ? "" : value.toString());
            }
            log.debug("获取哈希表所有字段值成功 key={}, values={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("获取哈希表所有字段值失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 将哈希表中指定字段的值增加指定整数
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @param delta 增加的数值
     * @return 增加后的值
     */
    @Override
    public Long incrBy(String key, String field, long delta) {
        try {
            Long result = redisTemplate.opsForHash().increment(key, field, delta);
            log.debug("哈希字段自增成功 key={}, field={}, delta={}, result={}", key, field, delta, result);
            return result;
        } catch (Exception e) {
            log.error("哈希字段自增失败 key={}, field={}, delta={}", key, field, delta, e);
            throw e;
        }
    }

    /**
     * 将哈希表中指定字段的值增加指定浮点数
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @param delta 增加的浮点数
     * @return 增加后的值
     */
    @Override
    public Double incrByFloat(String key, String field, double delta) {
        try {
            Double result = redisTemplate.opsForHash().increment(key, field, delta);
            log.debug("哈希字段浮点自增成功 key={}, field={}, delta={}, result={}", key, field, delta, result);
            return result;
        } catch (Exception e) {
            log.error("哈希字段浮点自增失败 key={}, field={}, delta={}", key, field, delta, e);
            throw e;
        }
    }

    /**
     * 当哈希表中指定字段不存在时才设置值
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @param value 字段值
     * @return 如果设置成功返回true，如果字段已存在返回false
     */
    @Override
    public boolean setIfAbsent(String key, String field, String value) {
        try {
            Boolean result = redisTemplate.opsForHash().putIfAbsent(key, field, value);
            log.debug("哈希字段不存在时设置成功 key={}, field={}, value={}, result={}", key, field, value, result);
            return result;
        } catch (Exception e) {
            log.error("哈希字段不存在时设置失败 key={}, field={}, value={}", key, field, value, e);
            throw e;
        }
    }

    /**
     * 获取哈希表中指定字段值的长度
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @return 字段值的长度
     */
    @Override
    public Long queryFieldLen(String key, String field) {
        try {
            Long length = redisTemplate.opsForHash().lengthOfValue(key, field);
            log.debug("获取哈希字段值长度成功 key={}, field={}, length={}", key, field, length);
            return length;
        } catch (Exception e) {
            log.error("获取哈希字段值长度失败 key={}, field={}", key, field, e);
            throw e;
        }
    }
}
