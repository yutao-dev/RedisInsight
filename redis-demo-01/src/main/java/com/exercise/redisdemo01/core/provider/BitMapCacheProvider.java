package com.exercise.redisdemo01.core.provider;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/23
 */
public interface BitMapCacheProvider {

    /**
     * 设置位图中指定偏移量的值
     *
     * @param key    位图的键
     * @param offset 偏移量
     * @param value  要设置的值（0 或 1）
     */
    void setBit(String key, long offset, boolean value);

    /**
     * 获取位图中指定偏移量的值
     *
     * @param key    位图的键
     * @param offset 偏移量
     * @return 指定偏移量的值
     */
    Boolean getBit(String key, long offset);

    /**
     * 统计位图中被设置为1的位数
     *
     * @param key 位图的键
     * @return 被设置为1的位数
     */
    Long bitCount(String key);

    /**
     * 查找位图中第一个被设置为指定值的位的位置
     *
     * @param key   位图的键
     * @param value 要查找的值（0 或 1）
     * @return 第一个被设置为指定值的位的位置，如果不存在则返回-1
     */
    Long bitPos(String key, boolean value);

    /**
     * 对一个或多个位图执行按位操作，并将结果存储到目标位图中
     *
     * @param operation 操作类型（AND, OR, XOR, NOT）
     * @param destKey   目标位图的键
     * @param keys      源位图的键列表
     */
    void bitOp(String operation, String destKey, String... keys);

    /**
     * 获取位图的内存使用量（字节）
     *
     * @param key 位图的键
     * @return 内存使用量（字节）
     */
    Long memoryUsage(String key);

    /**
     * 删除指定的位图键
     *
     * @param key 位图的键
     */
    void delete(String key);
}
