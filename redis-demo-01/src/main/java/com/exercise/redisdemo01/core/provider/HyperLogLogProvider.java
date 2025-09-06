package com.exercise.redisdemo01.core.provider;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/28
 */
public interface HyperLogLogProvider {
    /**
     * 向指定的 HyperLogLog 中添加元素
     *
     * @param key   HyperLogLog 的键名
     * @param value 要添加的元素值
     * @return 如果至少有一个新元素被添加则返回 true，否则返回 false
     */
    boolean save(String key, String value);

    /**
     * 获取指定 HyperLogLog 中不重复元素的估计数量
     *
     * @param key HyperLogLog 的键名
     * @return 不重复元素的估计数量
     */
    long querySize(String key);
}
