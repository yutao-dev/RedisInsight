package com.exercise.redisdemo01.core.provider;

import com.exercise.redisdemo01.core.model.bean.MemoryMetrics;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 缓存提供者，负责提供缓存的其他底层指令
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/10
 */
public interface CacheProvider {

    /**
     * 获取缓存提供者内存信息
     *
     * @return 缓存提供者内存信息键值对集合，如果获取失败则返回空Map
     */
     Map<String, String> infoMemory();

     /**
      * 初始化缓存提供者的内存信息
      */
     MemoryMetrics getMemoryMetrics();

     /**
      * 缓存提供者内存信息监控告警
      *
      * @param memoryMetrics 缓存提供者内存信息，仅进行基础的监控，不进行详细监控，需自行实现
      */
     default void monitoringAlarms(MemoryMetrics memoryMetrics) {
         memoryMetrics.showAll();
     }
}
