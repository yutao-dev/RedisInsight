package com.exercise.redisdemo01.core.provider;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/23
 */
public interface SetCacheProvider {

    // Set 相关方法定义

    /**
     * 向指定的 Set 集合中添加一个或多个元素
     *
     * @param key    Set 集合的键
     * @param values 要添加的元素数组
     */
    void set(String key, List<String> values);

    /**
     * 批量设置多个 Set 集合的元素，参数格式为 key1, value1, key2, value2, ...
     *
     * @param keyValues 键值对数组，必须为偶数个
     * @throws IllegalArgumentException 当参数个数不是偶数时抛出
     */
    void set(String... keyValues);

    /**
     * 原子性地批量设置多个 Set 集合的元素，使用 Lua 脚本保证操作的原子性
     * 参数格式为 key1, value1, key2, value2, ...
     *
     * @param keyValues 键值对数组，必须为偶数个
     * @throws IllegalArgumentException 当参数个数不是偶数时抛出
     */
    void atomicitySet(String... keyValues);

    /**
     * 获取指定 Set 集合中的所有元素
     *
     * @param key Set 集合的键
     * @return Set 集合中的所有元素
     */
    Set<String> queryAll(String key);

    /**
     * 判断指定元素是否存在于 Set 集合中
     *
     * @param key   Set 集合的键
     * @param value 要检查的元素
     * @return 如果元素存在返回 true，否则返回 false
     */
    boolean isExist(String key, String value);

    /**
     * 随机弹出并移除 Set 集合中的一个元素
     *
     * @param key Set 集合的键
     * @return 被弹出的元素，如果集合为空则返回 null
     */
    String randomPop(String key);

    /**
     * 获取两个 Set 集合的交集
     *
     * @param key      第一个 Set 集合的键
     * @param otherKey 第二个 Set 集合的键
     * @return 两个集合的交集
     */
    Set<String> interSection(String key, String otherKey);

    /**
     * 获取多个 Set 集合的交集
     *
     * @param keys Set 集合键的集合
     * @return 多个集合的交集
     */
    Set<String> interSection(Collection<String> keys);

    /**
     * 获取多个 Set 集合的并集
     *
     * @param keys Set 集合键的集合
     * @return 多个集合的并集
     */
    Set<String> unionSection(Collection<String> keys);

    /**
     * 获取两个 Set 集合的并集
     *
     * @param key      第一个 Set 集合的键
     * @param otherKey 第二个 Set 集合的键
     * @return 两个集合的并集
     */
    Set<String> unionSection(String key, String otherKey);

    /**
     * 从 Set 集合中移除指定元素
     *
     * @param key   Set 集合的键
     * @param value 要移除的元素
     */
    void remove(String key, String value);

    // ZSet 相关方法定义

    /**
     * 向有序集合中添加元素及其分数
     *
     * @param key   有序集合的键
     * @param value 要添加的元素
     * @param score 元素的分数
     */
    void zAdd(String key, String value, double score);

    /**
     * 原子性地批量添加有序集合元素，使用 Lua 脚本保证操作的原子性
     * 参数格式为 key1, value1, score1, key2, value2, score2, ...
     *
     * @param keyValues 键值对和分数数组，必须为3的倍数
     * @throws IllegalArgumentException 当参数个数不是3的倍数时抛出
     */
    void zAtomicityAdd(String... keyValues);

    /**
     * 批量添加有序集合元素
     * 参数格式为 key1, value1, score1, key2, value2, score2, ...
     *
     * @param keyValues 键值对和分数数组，必须为3的倍数
     * @throws IllegalArgumentException 当参数个数不是3的倍数时抛出
     */
    void zAdd(String... keyValues);

    /**
     * 从有序集合中移除一个或多个元素
     *
     * @param key    有序集合的键
     * @param values 要移除的元素数组
     */
    void zRemove(String key, Object... values);

    /**
     * 获取有序集合中指定范围的元素（按分数从小到大）
     *
     * @param key   有序集合的键
     * @param start 起始索引
     * @param end   结束索引
     * @return 指定范围内的元素集合
     */
    Set<String> zRange(String key, long start, long end);

    /**
     * 获取有序集合中指定范围的元素（按分数从大到小）
     *
     * @param key   有序集合的键
     * @param start 起始索引
     * @param end   结束索引
     * @return 指定范围内的元素集合
     */
    Set<String> zRevRange(String key, long start, long end);

    /**
     * 获取有序集合中指定元素的分数
     *
     * @param key   有序集合的键
     * @param value 元素值
     * @return 元素的分数，如果元素不存在则返回 null
     */
    Double zScore(String key, Object value);

    /**
     * 获取有序集合中指定元素的排名（按分数从小到大）
     *
     * @param key   有序集合的键
     * @param value 元素值
     * @return 元素的排名，如果元素不存在则返回 null
     */
    Long zRank(String key, Object value);

    /**
     * 获取有序集合中指定元素的排名（按分数从大到小）
     *
     * @param key   有序集合的键
     * @param value 元素值
     * @return 元素的排名，如果元素不存在则返回 null
     */
    Long zRevRank(String key, Object value);

    /**
     * 获取有序集合的元素个数
     *
     * @param key 有序集合的键
     * @return 集合中的元素个数
     */
    Long zCard(String key);

    /**
     * 获取有序集合中指定分数范围内的元素
     *
     * @param key 有序集合的键
     * @param min 最小分数（包含）
     * @param max 最大分数（包含）
     * @return 指定分数范围内的元素集合
     */
    Set<String> zRangeByScore(String key, double min, double max);

    /**
     * 统计有序集合中指定分数范围内的元素数量
     *
     * @param key 有序集合的键
     * @param min 最小分数（包含）
     * @param max 最大分数（包含）
     * @return 指定分数范围内的元素数量
     */
    Long zCount(String key, double min, double max);
}
