package com.exercise.redisdemo01.core.provider;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/23
 */
public interface HashCacheProvider {

    /**
     * 设置哈希表中指定字段的值
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @param value 字段值
     */
    void set(String key, String field, String value);

    /**
     * 获取哈希表中指定字段的值
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @return 字段值，如果不存在则返回空字符串
     */
    String get(String key, String field);

    /**
     * 删除哈希表中一个或多个字段
     *
     * @param key    哈希表的键
     * @param fields 要删除的字段名数组
     * @return 被成功删除的字段数量
     */
    Long del(String key, String... fields);

    /**
     * 检查哈希表中指定字段是否存在
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @return 如果字段存在返回true，否则返回false
     */
    Boolean isExists(String key, String field);

    /**
     * 获取哈希表中字段的数量
     *
     * @param key 哈希表的键
     * @return 字段数量
     */
    Long getLength(String key);

    /**
     * 批量设置哈希表中的多个字段
     *
     * @param key 哈希表的键
     * @param map 包含字段和值的映射
     */
    void set(String key, java.util.Map<String, String> map);

    /**
     * 批量获取哈希表中多个字段的值
     *
     * @param key    哈希表的键
     * @param fields 字段名数组
     * @return 字段值列表
     */
    java.util.List<String> get(String key, String... fields);

    /**
     * 获取哈希表中所有的字段和值 (警惕大Key风险)
     *
     * @param key 哈希表的键
     * @return 包含所有字段和值的映射
     */
    java.util.Map<String, String> getAll(String key);

    /**
     * 通过扫描的方式分批获取哈希表中所有的字段和值，避免一次性加载大量数据导致内存问题
     *
     * @param key       哈希表的键
     * @param batchSize 每次扫描的批次大小
     * @return 包含所有字段和值的映射
     */
    java.util.Map<String, String> scanAll(String key, int batchSize);

    /**
     * 获取哈希表中所有的字段名 (警惕大Key风险)
     *
     * @param key 哈希表的键
     * @return 字段名集合
     */
    java.util.Set<String> getKeys(String key);

    /**
     * 获取哈希表中所有的字段值 (警惕大Key风险)
     *
     * @param key 哈希表的键
     * @return 字段值集合
     */
    java.util.List<String> getValues(String key);

    /**
     * 将哈希表中指定字段的值增加指定整数
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @param delta 增加的数值
     * @return 增加后的值
     */
    Long incrBy(String key, String field, long delta);

    /**
     * 将哈希表中指定字段的值增加指定浮点数
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @param delta 增加的浮点数
     * @return 增加后的值
     */
    Double incrByFloat(String key, String field, double delta);

    /**
     * 当哈希表中指定字段不存在时才设置值
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @param value 字段值
     * @return 如果设置成功返回true，如果字段已存在返回false
     */
    boolean setIfAbsent(String key, String field, String value);

    /**
     * 获取哈希表中指定字段值的长度
     *
     * @param key   哈希表的键
     * @param field 字段名
     * @return 字段值的长度
     */
    Long queryFieldLen(String key, String field);
}
