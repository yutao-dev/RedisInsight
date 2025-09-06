package com.exercise.redisdemo01.core;

import com.exercise.redisdemo01.core.provider.StreamCacheProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/19
 */
@Slf4j
@SpringBootTest
class RedisStreamTest {

    @Resource
    private StreamCacheProvider streamCacheProvider;

    @Test
    void testAddMessage() {
        streamCacheProvider.addMessage("test-stream", "key1", "value1");
    }

    @Test
    void testAddMessageMap() {
        streamCacheProvider.addMessageMap("test-stream", Map.of("key1", "value1", "key2", "value2"));
    }

    @Test
    void testAddMessages() {
        streamCacheProvider.addMessages("test-stream", "key1", "value1", "key2", "value2");
    }

    @Test
    void testReadAll() {
        streamCacheProvider.addMessages("test-stream", "key1", "value1", "key2", "value2");
        Map<String, HashMap<String, String>> result = streamCacheProvider.readAll("test-stream", String.class, String.class);
        log.info("Read all messages: {}", result);
    }

    @Test
    void testReadMapCount() {
        streamCacheProvider.addMessages("test-stream", "key1", "value1", "key2", "value2");
        List<Map<String, String>> result = streamCacheProvider.readMapCount("test-stream", 2, String.class, String.class);
        log.info("Read messages with count: {}", result);
    }

    @Test
    void testReadMapBlock() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                streamCacheProvider.addMessage("test-stream", "key1", "value1");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        List<Map<String, String>> result = streamCacheProvider.readMapBlock("test-stream", 5000, String.class, String.class);
        log.info("Read messages with block: {}", result);
    }

    @Test
    void testCreateGroupAndReadMessage() {
        String streamName = "test-group-stream";
        String groupName = "test-group";
        String consumerName = "test-consumer";

        // 创建消费者组
        try {
            streamCacheProvider.createGroup(streamName, groupName);
        } catch (Exception e) {
            log.warn("Consumer group might already exist: {}", e.getMessage());
        }

        // 添加消息
        streamCacheProvider.addMessage(streamName, "key1", "value1");

        // 读取消息
        Map<String, String> result = streamCacheProvider.readMessage(streamName, groupName, consumerName, String.class, String.class);
        log.info("Read message from group: {}", result);
    }
}
