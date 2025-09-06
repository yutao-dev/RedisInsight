package com.exercise.redisdemo01.core.provider.impl;

import com.exercise.redisdemo01.core.model.bean.MemoryMetrics;
import com.exercise.redisdemo01.core.model.bean.RedisMemoryMetrics;
import com.exercise.redisdemo01.core.provider.CacheProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于Redis实现的缓存提供操作类
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRedisProvider implements CacheProvider {

    private final StringRedisTemplate redisTemplate;

    /**
     * 获取Redis内存信息
     * 
     * @return Redis内存信息键值对集合，如果获取失败则返回空Map
     */
    @Override
    public Map<String, String> infoMemory() {
        log.debug("开始获取Redis内存信息");
        
        try {
            Properties properties = redisTemplate.execute((RedisCallback<Properties>)
                    connection -> connection.serverCommands().info("memory")
            );
            
            if (Objects.isNull(properties)) {
                log.warn("获取Redis内存信息为空");
                return Collections.emptyMap();
            }
            
            log.debug("成功获取Redis内存信息，共{}条记录", properties.size());
            
            return properties.entrySet().stream()
                    .collect(
                            Collectors.toMap(
                                    property -> (String) property.getKey(),
                                    property -> (String) property.getValue()
                            )
                    );
        } catch (Exception e) {
            log.error("获取Redis内存信息时发生异常", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 初始化Redis内存信息
     */
    @Override
    public MemoryMetrics getMemoryMetrics() {
        return new RedisMemoryMetrics(infoMemory());
    }

    /**
     * 缓存提供者内存信息监控告警
     *
     * @param memoryMetrics 缓存提供者内存信息，进行精准监控
     */
    @Override
    public void monitoringAlarms(MemoryMetrics memoryMetrics) {
        String monitorAlerts = memoryMetrics.monitorAlerts();
        if (!monitorAlerts.isEmpty()) {
            log.warn("缓存提供者内存信息监控告警: {}", monitorAlerts);
            return;
        }
        log.info("缓存提供者内存信息监控正常");
    }
}
