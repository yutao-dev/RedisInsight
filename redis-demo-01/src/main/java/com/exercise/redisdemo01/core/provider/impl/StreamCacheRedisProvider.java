package com.exercise.redisdemo01.core.provider.impl;

import com.exercise.redisdemo01.core.provider.StreamCacheProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

/**
 * Redis Stream 消息队列提供者实现类
 * 用于操作 Redis Stream 数据结构，支持消息的添加、读取、确认等操作
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StreamCacheRedisProvider implements StreamCacheProvider {

    private final StringRedisTemplate redisTemplate;

    /**
     * 向指定的 Stream 中添加单个消息
     *
     * @param streamName 流名称，不能为空
     * @param key        消息键，不能为空
     * @param value      消息值，不能为空
     * @throws IllegalArgumentException 如果参数为空或无效
     * @throws RuntimeException         如果 Redis 操作失败
     */
    @Override
    public void addMessage(String streamName, String key, String value) {
        try {
            log.debug("准备向 Stream [{}] 添加消息: key={}, value={}", streamName, key, value);
            RecordId recordId = redisTemplate.opsForStream().add(streamName, Map.of(key, value));
            String messageId = Objects.isNull(recordId) ? null : recordId.getValue();
            logInfoAddMessage(streamName, messageId);
        } catch (Exception e) {
            log.error("向 Stream [{}] 添加消息失败: key={}, value={}", streamName, key, value, e);
            throw new RuntimeException("添加消息到 Stream 失败", e);
        }
    }

    /**
     * 向指定的 Stream 中添加多个键值对消息
     *
     * @param streamName 流名称，不能为空
     * @param map        消息键值对集合，不能为空
     * @throws IllegalArgumentException 如果参数为空或无效
     * @throws RuntimeException         如果 Redis 操作失败
     */
    @Override
    public void addMessageMap(String streamName, Map<String, String> map) {
        try {
            log.debug("准备向 Stream [{}] 添加 {} 个消息键值对", streamName, map.size());
            RecordId recordId = redisTemplate.opsForStream().add(streamName, map);
            String messageId = Objects.isNull(recordId) ? null : recordId.getValue();
            logInfoAddMessage(streamName, messageId);
        } catch (Exception e) {
            log.error("向 Stream [{}] 添加消息失败，map大小: {}", streamName, map.size(), e);
            throw new RuntimeException("添加消息到 Stream 失败", e);
        }
    }

    /**
     * 添加消息成功后，记录日志信息
     *
     * @param streamName 流名称
     * @param messageId  消息ID
     */
    private void logInfoAddMessage(String streamName, String messageId) {
        log.info("成功向 Stream [{}] 添加消息，消息ID: {}", streamName, messageId);
    }

    /**
     * 向指定的 Stream 中添加多个键值对消息（可变参数形式）
     * 参数必须成对出现，即键后必须跟对应的值
     *
     * @param streamName 流名称，不能为空
     * @param keyValue   键值对参数，必须为偶数个
     * @throws IllegalArgumentException 如果参数个数不是偶数或参数为空
     * @throws RuntimeException         如果 Redis 操作失败
     */
    @Override
    public void addMessages(String streamName, String... keyValue) {
        try {
            int length = keyValue.length;
            if (length % 2 != 0) {
                log.error("添加到消息队列的参数个数必须是偶数，当前个数: {}", length);
                throw new IllegalArgumentException("参数个数必须为偶数");
            }

            log.debug("准备向 Stream [{}] 添加 {} 个键值对", streamName, length / 2);
            HashMap<String, String> keyValueMap = new HashMap<>();
            for (int i = 0; i < length; i += 2) {
                keyValueMap.put(keyValue[i], keyValue[i + 1]);
            }

            RecordId recordId = redisTemplate.opsForStream().add(streamName, keyValueMap);
            String messageId = Objects.isNull(recordId) ? null : recordId.getValue();
            log.info("成功向 Stream [{}] 添加消息，消息ID: {}，共 {} 个键值对", streamName, messageId, keyValueMap.size());
        } catch (Exception e) {
            log.error("向 Stream [{}] 添加消息失败", streamName, e);
            throw new RuntimeException("添加消息到 Stream 失败", e);
        }
    }

    /**
     * 读取指定 Stream 中的所有消息，并按消息ID组织返回
     *
     * @param streamName 消息队列名称，不能为空
     * @param clazzK     key类型，用于类型转换
     * @param clazzV     value类型，用于类型转换
     * @return 包含所有消息的映射，key为消息ID，value为消息内容映射
     * @throws RuntimeException 如果 Redis 操作失败或类型转换异常
     */
    @Override
    public <K, V> Map<String, HashMap<K, V>> readAll(String streamName, Class<K> clazzK, Class<V> clazzV) {
        try {
            log.debug("开始读取 Stream [{}] 的所有消息", streamName);
            List<MapRecord<String, Object, Object>> mapRecords = redisTemplate.opsForStream().read(StreamOffset.fromStart(streamName));
            logReadMessage(streamName, mapRecords);

            Map<String, HashMap<K, V>> messageMap = new HashMap<>();

            for (MapRecord<String, Object, Object> mapRecord : mapRecords) {
                String messageId = mapRecord.getId().getValue();
                Map<Object, Object> mapRecordValue = mapRecord.getValue();

                if (!mapRecordValue.isEmpty()) {
                    HashMap<K, V> bodyMap = new HashMap<>();
                    try {
                        mapRecordValue.entrySet().stream()
                                .map(entry -> {
                                    K key = clazzK.cast(entry.getKey());
                                    V value = clazzV.cast(entry.getValue());
                                    return new AbstractMap.SimpleEntry<>(key, value);
                                })
                                .forEach(entry -> bodyMap.put(entry.getKey(), entry.getValue()));

                        messageMap.put(messageId, bodyMap);
                    } catch (ClassCastException e) {
                        log.error("类型转换异常，messageId: {}, error: {}", messageId, e.getMessage(), e);
                    }
                }
            }
            log.debug("完成读取 Stream [{}] 的所有消息，共 {} 条消息", streamName, messageMap.size());
            return messageMap;
        } catch (Exception e) {
            log.error("读取 Stream [{}] 所有消息失败", streamName, e);
            throw new RuntimeException("读取 Stream 消息失败", e);
        }
    }

    /**
     * 读取指定 Stream 中的消息，并转换为指定类型的映射
     *
     * @param streamName 流名称，不能为空
     * @param mapRecords 读取的 MapRecord 列表
     */
    private void logReadMessage(String streamName, List<MapRecord<String, Object, Object>> mapRecords) {
        if (Objects.isNull(mapRecords)) {
            log.info("未从 Stream [{}] 读取到消息记录", streamName);
        } else {
            log.info("从 Stream [{}] 读取到 {} 条消息记录", streamName, mapRecords.size());
        }
    }

    /**
     * 根据配置读取 Stream 中的消息
     *
     * @param streamName  流名称
     * @param isCount     是否限制读取数量
     * @param count       读取数量上限
     * @param isBlock     是否阻塞读取
     * @param blockTime   阻塞等待时间（毫秒）
     * @param clazzK      key类型
     * @param clazzV      value类型
     * @return 转换后的消息列表
     */
    private <K, V> List<Map<K, V>> readMap(String streamName, boolean isCount, int count, boolean isBlock, long blockTime, Class<K> clazzK, Class<V> clazzV) {
        try {
            log.debug("开始读取 Stream [{}] 消息: isCount={}, count={}, isBlock={}, blockTime={}", 
                    streamName, isCount, count, isBlock, blockTime);

            StreamReadOptions streamReadOptions = isCount ? StreamReadOptions.empty().count(count) : StreamReadOptions.empty();
            streamReadOptions = isBlock ? streamReadOptions.block(Duration.ofMillis(blockTime)) : streamReadOptions;
            StreamOffset<String> stringStreamOffset = StreamOffset.latest(streamName);

            List<MapRecord<String, Object, Object>> mapRecords = redisTemplate.opsForStream().read(streamReadOptions, stringStreamOffset);
            logReadMessage(streamName, mapRecords);

            List<Map<K, V>> result = castMapToList(clazzK, clazzV, mapRecords);
            log.debug("完成读取 Stream [{}] 消息，转换后共 {} 条", streamName, result.size());
            return result;
        } catch (Exception e) {
            log.error("读取 Stream [{}] 消息失败: isCount={}, count={}, isBlock={}, blockTime={}", 
                    streamName, isCount, count, isBlock, blockTime, e);
            throw new RuntimeException("读取 Stream 消息失败", e);
        }
    }

    /**
     * 将 Redis 返回的消息记录转换为指定类型的映射列表
     *
     * @param clazzK      key类型
     * @param clazzV      value类型
     * @param mapRecords  Redis 返回的消息记录列表
     * @return 转换后的映射列表
     */
    private static <K, V> List<Map<K, V>> castMapToList(Class<K> clazzK, Class<V> clazzV, List<MapRecord<String, Object, Object>> mapRecords) {
        List<Map<K, V>> messageMaps = new ArrayList<>();
        if (Objects.nonNull(mapRecords)) {
            log.debug("开始转换 {} 条消息记录", mapRecords.size());
            for (MapRecord<String, Object, Object> mapRecord : mapRecords) {
                mapRecord.getValue().forEach((key, value) -> {
                    try {
                        HashMap<K, V> hashMap = new HashMap<>();
                        hashMap.put(clazzK.cast(key), clazzV.cast(value));
                        messageMaps.add(hashMap);
                    } catch (Exception e) {
                        log.error("类型转换错误！key类型: {}, value类型: {}, 实际key: {}, 实际value: {}", 
                                clazzK.getName(), clazzV.getName(), key, value, e);
                    }
                });
            }
            log.debug("完成转换，共 {} 条消息映射", messageMaps.size());
        }
        return messageMaps;
    }

    /**
     * 读取指定数量的消息
     *
     * @param streamName 流名称
     * @param count      读取数量
     * @param clazzK     key类型
     * @param clazzV     value类型
     * @return 消息列表
     */
    @Override
    public <K, V> List<Map<K, V>> readMapCount(String streamName, int count, Class<K> clazzK, Class<V> clazzV) {
        log.debug("调用 readMapCount: streamName={}, count={}", streamName, count);
        return readMap(streamName, true, count, false, 0, clazzK, clazzV);
    }

    /**
     * 阻塞式读取消息
     *
     * @param streamName 流名称
     * @param blockTime  阻塞等待时间（毫秒）
     * @param clazzK     key类型
     * @param clazzV     value类型
     * @return 消息列表
     */
    @Override
    public <K, V> List<Map<K, V>> readMapBlock(String streamName, long blockTime, Class<K> clazzK, Class<V> clazzV) {
        log.debug("调用 readMapBlock: streamName={}, blockTime={}", streamName, blockTime);
        return readMap(streamName, false, 0, true, blockTime, clazzK, clazzV);
    }

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
    @Override
    public <K, V> List<Map<K, V>> readMap(String streamName, int count, long blockTime, Class<K> clazzK, Class<V> clazzV) {
        log.debug("调用 readMap: streamName={}, count={}, blockTime={}", streamName, count, blockTime);
        return readMap(streamName, true, count, true, blockTime, clazzK, clazzV);
    }

    /**
     * 创建消费者组
     *
     * @param streamName 流名称
     * @param groupName  消费者组名称
     */
    @Override
    public void createGroup(String streamName, String groupName) {
        log.info("创建消费者组: streamName={}, groupName={}", streamName, groupName);
        try {
            redisTemplate.opsForStream().createGroup(streamName, groupName);
            log.info("成功创建消费者组: streamName={}, groupName={}", streamName, groupName);
        } catch (Exception e) {
            log.error("创建消费者组失败: streamName={}, groupName={}", streamName, groupName, e);
            throw new RuntimeException("创建消费者组失败", e);
        }
    }

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
    @Override
    public <K, V> Map<K, V> readMessage(String streamName, String groupName, String consumerName, Class<K> clazzK, Class<V> clazzV) {
        try {
            log.debug("从消费者组读取消息: streamName={}, groupName={}, consumerName={}", streamName, groupName, consumerName);
            StreamReadOptions streamReadOptions = StreamReadOptions.empty().count(1);
            Consumer consumer = Consumer.from(groupName, consumerName);
            StreamOffset<String> streamOffset = StreamOffset.create(streamName, ReadOffset.lastConsumed());
            List<MapRecord<String, Object, Object>> mapRecords = redisTemplate.opsForStream().read(consumer, streamReadOptions, streamOffset);
            log.info("从消费者组读取到 {} 条消息记录", mapRecords.size());

            List<Map<K, V>> mapList = castMapToList(clazzK, clazzV, mapRecords);
            Map<K, V> result = mapList.isEmpty() ? Collections.emptyMap() : mapList.get(0);
            log.debug("完成从消费者组读取消息，返回 {} 条消息", result.size());
            return result;
        } catch (Exception e) {
            log.error("从消费者组读取消息失败: streamName={}, groupName={}, consumerName={}", streamName, groupName, consumerName, e);
            throw new RuntimeException("从消费者组读取消息失败", e);
        }
    }

    /**
     * 确认消息已处理
     *
     * @param streamName  流名称
     * @param groupName   消费者组名称
     * @param messageIds  消息ID列表
     */
    @Override
    public void ackMessage(String streamName, String groupName, String... messageIds) {
        try {
            log.debug("确认消息已处理: streamName={}, groupName={}, messageIds={}", streamName, groupName, messageIds);
            redisTemplate.opsForStream().acknowledge(streamName, groupName, messageIds);
            log.info("成功确认 {} 条消息已处理: streamName={}, groupName={}", messageIds.length, streamName, groupName);
        } catch (Exception e) {
            log.error("确认消息已处理失败: streamName={}, groupName={}, messageIds={}", streamName, groupName, messageIds, e);
            throw new RuntimeException("确认消息已处理失败", e);
        }
    }
}
