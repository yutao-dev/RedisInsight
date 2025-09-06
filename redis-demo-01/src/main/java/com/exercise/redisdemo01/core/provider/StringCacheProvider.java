package com.exercise.redisdemo01.core.provider;

import java.util.Map;

/**
 * 缓存提供者，接口抽象专注于缓存
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/8
 */
public interface StringCacheProvider {

    /**
     * 设置缓存
     *
     * @param key   缓存key
     * @param value 缓存value
     */
    void setString(String key, String value);

    /**
     * 设置缓存, 当key不存在时才设置
     *
     * @param key   缓存key
     * @param value 缓存value
     * @return 是否设置成功
     */
    Boolean setStringWhenNotExists(String key, String value);

    /**
     * 设置缓存, 当key不存在时才设置, 默认过期时间单位是秒
     *
     * @param key   缓存key
     * @param value 缓存value
     * @param expire 过期时间
     * @return 是否设置成功
     */
    Boolean setStringWhenNotExists(String key, String value, long expire);

    /**
     * 获取并设置缓存, 默认返回旧值
     *
     * @param key   缓存key
     * @param value 缓存value
     * @return 旧值
     */
    String getAndSetString(String key, String value);

    /**
     * 带过期时间的设置缓存, 默认为秒
     *
     * @param key   缓存key
     * @param value 缓存value
     * @param expire 过期时间
     */
    void setString(String key, String value, long expire);

    /**
     * 获取缓存的过期时间
     *
     * @param key 缓存key
     * @return 过期时间
     */
    long ttlKey(String key);

    /**
     * 获取缓存, 如果不存在则返回空字符串
     *
     * @param key 缓存key
     * @return 缓存value
     */
    String getString(String key);

    /**
     * 自增缓存，无需get/set更加高效
     *
     * @param key 缓存key
     */
    void incrString(String key);

    /**
     * 自定义量自增缓存，无需get/set更加高效
     *
     * @param key   缓存key
     * @param value 自增数量
     */
    void incrString(String key, long value);

    /**
     * 自定义量自增缓存，无需get/set更加高效
     *
     * @param key   缓存key
     * @param value 自增数量
     */
    void incrString(String key, double value);

    /**
     * 自减缓存，无需get/set更加高效
     *
     * @param key 缓存key
     */
    void decrString(String key);

    /**
     * 自定义量自减缓存，无需get/set更加高效
     *
     * @param key   缓存key
     * @param value 自减数量
     */
    void decrString(String key, long value);

    /**
     * 批量设置缓存
     *
     * @param map 缓存map
     */
    void batchSetString(Map<String, String> map);

    /**
     * 批量设置缓存
     *
     * @param keysAndValues 缓存key和value
     */
    void batchSetString(String... keysAndValues);

    /**
     * 当缓存不存在时，批量设置缓存，需要保证所有的缓存都不存在，否则会失败
     *
     * @param map 缓存map
     */
    void batchSetWhenNotExists(Map<String, String> map);

    /**
     * 设置缓存的bit位
     *
     * @param key    缓存key
     * @param offset bit位
     * @param value  bit值
     */
    void setBitString(String key, long offset, boolean value);

    /**
     * 获取缓存的bit位
     *
     * @param key    缓存key
     * @param offset bit位
     * @return bit值
     */
    Boolean getBitString(String key, long offset);

    /**
     * 删除缓存
     *
     * @param key 缓存key
     */
    void delete(String key);
}
