package com.exercise.redisdemo01.core.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/15
 */
public interface StreamCacheProvider {

    /**
     * 向指定的 Stream 中添加单个消息
     *
     * @param streamName 流名称，不能为空
     * @param key        消息键，不能为空
     * @param value      消息值，不能为空
     * @throws IllegalArgumentException 如果参数为空或无效
     * @throws RuntimeException         如果 Redis 操作失败
     */
    void addMessage(String streamName, String key, String value);

    /**
     * 向指定的 Stream 中添加多个键值对消息
     *
     * @param streamName 流名称，不能为空
     * @param map        消息键值对集合，不能为空
     * @throws IllegalArgumentException 如果参数为空或无效
     * @throws RuntimeException         如果 Redis 操作失败
     */
    void addMessageMap(String streamName, java.util.Map<String, String> map);

    /**
     * 向指定的 Stream 中添加多个键值对消息（可变参数形式）
     * 参数必须成对出现，即键后必须跟对应的值
     *
     * @param streamName 流名称，不能为空
     * @param keyValue   键值对参数，必须为偶数个
     * @throws IllegalArgumentException 如果参数个数不是偶数或参数为空
     * @throws RuntimeException         如果 Redis 操作失败
     */
    void addMessages(String streamName, String... keyValue);

    /**
     * 读取指定 Stream 中的所有消息，并按消息ID组织返回
     *
     * @param streamName 消息队列名称，不能为空
     * @param clazzK     key类型，用于类型转换
     * @param clazzV     value类型，用于类型转换
     * @return 包含所有消息的映射，key为消息ID，value为消息内容映射
     * @throws RuntimeException 如果 Redis 操作失败或类型转换异常
     */
    <K, V> Map<String, HashMap<K, V>> readAll(String streamName, Class<K> clazzK, Class<V> clazzV);

    /**
     * 读取指定数量的消息
     *
     * @param streamName 流名称
     * @param count      读取数量
     * @param clazzK     key类型
     * @param clazzV     value类型
     * @return 消息列表
     */
    <K, V> List<Map<K, V>> readMapCount(String streamName, int count, Class<K> clazzK, Class<V> clazzV);

    /**
     * 阻塞式读取消息
     *
     * @param streamName 流名称
     * @param blockTime  阻塞等待时间（毫秒）
     * @param clazzK     key类型
     * @param clazzV     value类型
     * @return 消息列表
     */
    <K, V> List<Map<K, V>> readMapBlock(String streamName, long blockTime, Class<K> clazzK, Class<V> clazzV);

    /**
     * 读取指定数量的消息并支持阻塞
     *
     * @param streamName 流名称
     * @param count      读取数量
     * @param blockTime  阻塞等待时间（毫秒）
     * @param clazzK     key类型
     * @param clazzV     value类型
     * @return 消息列表
     */
    <K, V> List<Map<K, V>> readMap(String streamName, int count, long blockTime, Class<K> clazzK, Class<V> clazzV);

    /**
     * 创建消费者组
     *
     * @param streamName 流名称
     * @param groupName  消费者组名称
     */
    void createGroup(String streamName, String groupName);

    /**
     * 从消费者组中读取消息
     *
     * @param streamName   流名称
     * @param groupName    消费者组名称
     * @param consumerName 消费者名称
     * @param clazzK       key类型
     * @param clazzV       value类型
     * @return 消息映射
     */
    <K, V> Map<K, V> readMessage(String streamName, String groupName, String consumerName, Class<K> clazzK, Class<V> clazzV);

    /**
     * 确认消息已处理
     *
     * @param streamName  流名称
     * @param groupName   消费者组名称
     * @param messageIds  消息ID列表
     */
    void ackMessage(String streamName, String groupName, String... messageIds);
}
