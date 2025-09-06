package com.exercise.redisdemo01.core.provider;

import java.util.Collection;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/28
 */
public interface BloomFilterProvider {
    
    /**
     * 向布隆过滤器中添加单个元素
     * 
     * @param key 布隆过滤器的键名
     * @param value 要添加的值
     * @return 添加成功返回true，失败返回false
     */
    boolean save(String key, String value);
    
    /**
     * 向布隆过滤器中批量添加元素
     * 
     * @param key 布隆过滤器的键名
     * @param values 要添加的值集合
     * @return 添加成功返回true，失败返回false
     */
    boolean save(String key, Collection<String> values);
    
    /**
     * 判断布隆过滤器中是否存在指定元素
     * 
     * @param key 布隆过滤器的键名
     * @param value 要查询的值
     * @return 存在返回true，不存在返回false
     */
    boolean contains(String key, String value);
    
    /**
     * 判断布隆过滤器中是否存在指定的多个元素
     * 
     * @param key 布隆过滤器的键名
     * @param values 要查询的值集合
     * @return 所有元素都存在返回true，否则返回false
     */
    boolean contains(String key, Collection<String> values);
}
