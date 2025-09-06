package com.exercise.redisdemo01.core.provider.impl;

import com.exercise.redisdemo01.core.provider.StringCacheProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis实现的缓存提供者
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/8
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StringCacheRedisProvider implements StringCacheProvider {

    /**
     * Redis模板
     */
    private final StringRedisTemplate redisTemplate;

    /**
     * 设置缓存
     *
     * @param key   缓存key
     * @param value 缓存value
     */
    @Override
    public void setString(String key, String value) {
        try {
            checkSize(key, value);
            redisTemplate.opsForValue().set(key, value);
            log.debug("设置缓存成功 key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("直接设置缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 设置缓存, 当key不存在时才设置
     *
     * @param key   缓存key
     * @param value 缓存value
     * @return 是否设置成功
     */
    @Override
    public Boolean setStringWhenNotExists(String key, String value) {
        try {
            checkSize(key, value);
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value);
            log.debug("设置缓存成功 key={}, value={}, result={}", key, value, result);
            return result;
        } catch (Exception e) {
            logSetStringError(key, value, e);
            throw e;
        }
    }

    private static void logSetStringError(String key, String value, Exception e) {
        log.error("设置缓存失败 key={}, value={}", key, value, e);
    }

    /**
     * 设置缓存, 当key不存在时才设置, 默认过期时间单位是秒
     *
     * @param key   缓存key
     * @param value 缓存value
     * @return 是否设置成功
     */
    @Override
    public Boolean setStringWhenNotExists(String key, String value, long expire) {
        try {
            checkSize(key, value);
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);
            log.debug("设置缓存成功 key={}, value={}, result={}, expire={}s", key, value, result, expire);
            return result;
        } catch (Exception e) {
            logSetStringError(key, value, e);
            throw e;
        }
    }

    /**
     * 获取并设置缓存, 默认返回旧值
     *
     * @param key   缓存key
     * @param value 缓存value
     * @return 旧值
     */
    @Override
    public String getAndSetString(String key, String value) {
        try {
            checkSize(key, value);
            String result = redisTemplate.opsForValue().getAndSet(key, value);
            log.debug("获取并设置缓存成功 key={}, value={}, result={}", key, value, result);
            return result;
        } catch (Exception e) {
            logSetStringError(key, value, e);
            throw e;
        }
    }

    /**
     * 检查缓存大小是否超过1MB
     * @param key 缓存key
     * @param value 缓存value
     */
    private static void checkSize(String key, String value) {
        if (value.length() > 1024 * 1024) {
            log.warn("value大小超过1MB，拒绝写入,key={},value={},size={}", key, value, value.length());
        }
    }

    /**
     * 带过期时间的设置缓存, 默认为秒
     *
     * @param key   缓存key
     * @param value 缓存value
     * @param expire 过期时间
     */
    @Override
    public void setString(String key, String value, long expire) {
        try {
            checkSize(key, value);
            redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
            log.debug("设置带过期时间缓存成功 key={}, value={}, expire={}", key, value, expire);
        } catch (Exception e) {
            log.error("设置带过期时间缓存失败 key={}, value={}, expire={}", key, value, expire, e);
            throw e;
        }
    }

    /**
     * 获取缓存的过期时间
     *
     * @param key 缓存key
     * @return 过期时间
     */
    @Override
    public long ttlKey(String key) {
        try {
            long expire = redisTemplate.getExpire(key);
            log.debug("获取缓存过期时间 key={}, expire={}", key, expire);
            return expire;
        } catch (Exception e) {
            log.error("获取缓存过期时间失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 获取缓存, 如果不存在则返回空字符串
     *
     * @param key 缓存key
     * @return 缓存value
     */
    @Override
    public String getString(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            String result = Objects.isNull(value) ? "" : value;
            log.debug("获取缓存 key={}, value={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("获取缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 自增缓存，无需get/set更加高效
     *
     * @param key 缓存key
     */
    @Override
    public void incrString(String key) {
        try {
            redisTemplate.opsForValue().increment(key);
            log.debug("自增缓存 key={}", key);
        } catch (Exception e) {
            log.error("自增缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 自定义量自增缓存，无需get/set更加高效
     *
     * @param key 缓存key
     * @param value 自增数量
     */
    @Override
    public void incrString(String key, long value) {
        try {
            redisTemplate.opsForValue().increment(key, value);
            log.debug("自定义量自增缓存 key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("自定义量自增缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 自定义量自增缓存，无需get/set更加高效
     *
     * @param key 缓存key
     * @param value 自增数量
     */
    @Override
    public void incrString(String key, double value) {
        try {
            redisTemplate.opsForValue().increment(key, value);
            log.debug("自定义量自增缓存(double) key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("自定义量自增缓存(double)失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 自减缓存，无需get/set更加高效
     *
     * @param key 缓存key
     */
    @Override
    public void decrString(String key) {
        try {
            redisTemplate.opsForValue().decrement(key);
            log.debug("自减缓存 key={}", key);
        } catch (Exception e) {
            log.error("自减缓存失败 key={}", key, e);
            throw e;
        }
    }

    /**
     * 自定义量自减缓存，无需get/set更加高效
     *
     * @param key 缓存key
     * @param value 自减数量
     */
    @Override
    public void decrString(String key, long value) {
        try {
            redisTemplate.opsForValue().decrement(key, value);
            log.debug("自定义量自减缓存 key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("自定义量自减缓存失败 key={}, value={}", key, value, e);
            throw e;
        }
    }

    /**
     * 批量设置缓存
     *
     * @param map 缓存map
     */
    public void batchSetString(Map<String, String> map) {
        try {
            redisTemplate.opsForValue().multiSet(map);
            log.debug("批量设置缓存成功 map={}", map);
        } catch (Exception e) {
            log.error("批量设置缓存失败 map={}", map, e);
            throw e;
        }
    }

    /**
     * 批量设置缓存
     *
     * @param keysAndValues 缓存key和value
     */
    @Override
    public void batchSetString(String... keysAndValues) {
        try {
            int length = keysAndValues.length;
            if (length % 2 != 0) {
                throw new IllegalArgumentException("参数长度必须为偶数");
            }
            HashMap<String, String> stringHashMap = new HashMap<>(length / 2);
            for (int i = 0; i < length; i += 2) {
                stringHashMap.put(keysAndValues[i], keysAndValues[i + 1]);
            }
            redisTemplate.opsForValue().multiSet(stringHashMap);
            log.debug("批量设置缓存成功 keysAndValues={}", (Object) keysAndValues);
        } catch (Exception e) {
            log.error("批量设置缓存失败 keysAndValues={}", (Object) keysAndValues, e);
            throw e;
        }
    }

    /**
     * 当缓存不存在时，批量设置缓存
     *
     * @param map 缓存map
     */
    @Override
    public void batchSetWhenNotExists(Map<String, String> map) {
        try {
            redisTemplate.opsForValue().multiSetIfAbsent(map);
            log.debug("当缓存不存在，批量设置缓存成功 map={}", map);
        } catch (Exception e) {
            log.error("批量设置缓存失败 map={}", map, e);
            throw e;
        }
    }

    /**
     * 设置缓存的bit位
     *
     * @param key 缓存key
     * @param offset bit位
     * @param value bit值
     */
    @Override
    public void setBitString(String key, long offset, boolean value) {
        try {
            redisTemplate.opsForValue().setBit(key, offset, value);
            log.debug("设置缓存bit位 key={}, offset={}, value={}", key, offset, value);
        } catch (Exception e) {
            log.error("设置缓存bit位失败 key={}, offset={}, value={}", key, offset, value, e);
            throw e;
        }
    }

    /**
     * 获取缓存的bit位
     *
     * @param key 缓存key
     * @param offset bit位
     * @return bit值
     */
    @Override
    public Boolean getBitString(String key, long offset) {
        try {
            Boolean result = redisTemplate.opsForValue().getBit(key, offset);
            log.debug("获取缓存bit位 key={}, offset={}, result={}", key, offset, result);
            return result;
        } catch (Exception e) {
            log.error("获取缓存bit位失败 key={}, offset={}", key, offset, e);
            throw e;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存key
     */
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
