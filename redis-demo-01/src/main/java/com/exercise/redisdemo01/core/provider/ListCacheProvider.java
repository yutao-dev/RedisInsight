package com.exercise.redisdemo01.core.provider;

/**
 * 专注于List类型的提供者，解耦操作细节
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/9
 */
public interface ListCacheProvider {

    /**
     * 从列表左侧（头部）插入一个元素
     *
     * @param key   缓存key，不能为空
     * @param value 要插入的值，不能为空
     */
    void leftPush(String key, String value);

    /**
     * 从列表右侧（尾部）插入一个元素
     *
     * @param key   缓存key，不能为空
     * @param value 要插入的值，不能为空
     */
    void rightPush(String key, String value);

    /**
     * 从列表左侧（头部）弹出一个元素，如果列表为空则返回null
     *
     * @param key 缓存key，不能为空
     * @return 弹出的元素值，可能为null
     */
    String leftPop(String key);

    /**
     * 从列表右侧（尾部）弹出一个元素，如果列表为空则返回null
     *
     * @param key 缓存key，不能为空
     * @return 弹出的元素值，可能为null
     */
    String rightPop(String key);

    /**
     * 获取指定key对应列表中的所有元素
     *
     * @param key 缓存key，不能为空
     * @return 所有元素组成的列表，不会为null
     */
    java.util.List<String> getAll(String key);

    /**
     * 获取指定key对应列表中指定范围的元素
     *
     * @param key   缓存key，不能为空
     * @param start 开始索引（包含），从0开始
     * @param end   结束索引（包含），-1表示最后一个元素
     * @return 指定范围内的元素组成的列表，不会为null
     */
    java.util.List<String> getRange(String key, long start, long end);

    /**
     * 获取指定key对应列表中指定索引位置的元素
     *
     * @param key   缓存key，不能为空
     * @param index 索引位置，支持负数（-1表示最后一个元素）
     * @return 指定索引位置的元素值，如果不存在则返回空字符串
     */
    String getIndex(String key, long index);

    /**
     * 获取指定key对应列表中的元素个数
     *
     * @param key 缓存key，不能为空
     * @return 列表元素个数，如果key不存在则返回0
     */
    Long getLength(String key);

    /**
     * 删除指定key对应的缓存
     *
     * @param key 缓存key，不能为空
     */
    void delete(String key);
}
