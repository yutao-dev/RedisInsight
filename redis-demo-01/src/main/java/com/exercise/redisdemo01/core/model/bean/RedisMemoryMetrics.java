package com.exercise.redisdemo01.core.model.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Redis内存监控指标对象，封装了Redis内存使用的关键指标信息
 * 用于监控和分析Redis实例的内存使用情况，帮助进行性能调优和容量规划
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/10
 */
@Slf4j
@Data
public class RedisMemoryMetrics implements MemoryMetrics {

    // 风险阈值常量
    private static final double FRAGMENTATION_RISK_THRESHOLD = 15.0;
    private static final double EVICTION_RISK_THRESHOLD = 20.0;
    private static final double MEM_FRAGMENTATION_RATIO_THRESHOLD = 1.5;
    private static final int MEMORY_PERCENT_THRESHOLD = 85;

    /**
     * Redis分配器分配的内存总量(以字节为单位)
     * 该值包括了所有数据、内部开销和碎片等的总和
     */
    private Long usedMemory;

    /**
     * Redis进程使用的物理内存总量(以字节为单位)
     * 包括Redis进程使用的全部物理内存，可能大于used_memory
     */
    private Long usedMemoryRss;

    /**
     * 内存碎片比率
     * 计算公式: used_memory_rss / used_memory
     * 正常情况下应接近1.0，过高表示存在内存碎片问题
     */
    private Double memFragmentationRatio;

    /**
     * Redis配置的最大内存限制(以字节为单位)
     * 当达到该限制时，Redis会根据max memory-policy策略进行数据淘汰
     */
    private Long maxMemory;

    /**
     * 因内存不足而被驱逐的键数量
     * 反映了因内存限制而丢失的数据量，用于评估内存配置是否合理
     */
    private Long evictedKeys;

    /**
     * 数据集使用的内存量(以字节为单位)
     * 计算公式: used_memory - used_memory_startup
     * 表示实际存储数据所占用的内存量，不包括Redis内部开销
     */
    private Long usedMemoryDataset;

    /**
     * Redis启动时使用的内存量(以MB为单位)
     * 启动时Redis使用的内存量，用于计算已使用的内存量
     */
    private Long usedMemoryMb;

    /**
     * Redis配置的最大内存限制(以MB为单位)
     * 配置的Redis最大内存限制，用于计算已使用的内存占比
     */
    private Long maxMemoryMb;

    /**
     * Redis进程使用的物理内存总量(以MB为单位)
     * 测量Redis进程使用的物理内存，用于计算已使用的内存占比
     */
    private Long usedMemoryRssMb;

    /**
     * 数据集使用的内存量(以MB为单位)
     * 测量数据集使用的内存量，用于计算已使用的内存占比
     */
    private Long usedMemoryDatasetMb;

    /**
     * 内存使用率(精确到小数点后两位)
     * 用于风险控制等需要更高精度的场景
     */
    private Double memoryUsageRate;

    /**
     * 内存碎片率风险等级
     * 用于风险评估，值越高风险越大
     */
    private Double fragmentationRiskLevel;

    /**
     * 内存驱逐风险指标
     * 基于驱逐键数量计算的风险值，用于预测内存压力
     */
    private Double evictionRiskIndicator;

    // 使用DecimalFormat替代String.format提高性能
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public RedisMemoryMetrics(Map<String, String> infoMemory) {
        if (infoMemory == null || infoMemory.isEmpty()) {
            log.warn("传入的infoMemory为空，无法初始化Redis内存监控指标");
            return;
        }
        
        // 初始化基础内存指标
        this.usedMemory = parseLongValue(infoMemory.get("used_memory"));
        this.usedMemoryRss = parseLongValue(infoMemory.get("used_memory_rss"));
        this.maxMemory = parseLongValue(infoMemory.get("maxmemory"));
        this.evictedKeys = parseLongValue(
                Objects.isNull(infoMemory.get("evicted_keys")) ? "0" : infoMemory.get("evicted_keys")
        );
        this.usedMemoryDataset = parseLongValue(infoMemory.get("used_memory_dataset"));
        
        // 初始化内存比率指标
        this.memFragmentationRatio = parseDoubleValue(infoMemory.get("mem_fragmentation_ratio"));
        
        // 初始化MB单位的内存指标
        initUsedAndMaxMemory();
    }
    
    /**
     * 安全解析字符串为Long类型
     * 
     * @param value 字符串值
     * @return 解析后的Long值，解析失败返回null
     */
    private Long parseLongValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("解析Long值失败: {}", value, e);
            return null;
        }
    }
    
    /**
     * 安全解析字符串为Double类型
     * 
     * @param value 字符串值
     * @return 解析后的Double值，解析失败返回null
     */
    private Double parseDoubleValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("解析Double值失败: {}", value, e);
            return null;
        }
    }

    /**
     * 初始化已使用内存和最大内存
     */
    private void initUsedAndMaxMemory() {
        if (usedMemoryMb == null && usedMemory != null) {
            usedMemoryMb = usedMemory / (1024 * 1024);
        }
        if (maxMemoryMb == null && maxMemory != null) {
            maxMemoryMb = maxMemory / (1024 * 1024);
        }
        if (usedMemoryRssMb == null && usedMemoryRss != null) {
            usedMemoryRssMb = usedMemoryRss / (1024 * 1024);
        }
        if (usedMemoryDatasetMb == null && usedMemoryDataset != null) {
            usedMemoryDatasetMb = usedMemoryDataset / (1024 * 1024);
        }
    }

    /**
     * 查询已使用内存占比
     * 返回已使用内存的百分比，并打印日志进行监控
     *
     * @return 已使用内存的百分比
     */
    @Override
    public int queryUsedMemoryPercent() {
        if (usedMemory == null || usedMemory == 0 || maxMemory == null || maxMemory == 0) {
            log.warn("usedMemory或maxMemory为null或0(无限制)，无法计算已使用内存占比");
            return 0;
        }
        int percent = (int) (100L * usedMemory / maxMemory);
        if (percent > MEMORY_PERCENT_THRESHOLD) {
            log.warn("已使用内存占比过高: {}%", percent);
        } else {
            log.info("已使用内存占比正常: {}%", percent);
        }
        return percent;
    }

    /**
     * 查询高精度的内存使用率
     * 返回精确到小数点后两位的内存使用率
     *
     * @return 高精度内存使用率
     */
    @Override
    public double queryMemoryUsageRate() {
        if (memoryUsageRate != null) {
            return memoryUsageRate;
        }
        
        if (usedMemory == null || maxMemory == null || maxMemory == 0) {
            log.warn("usedMemory或maxMemory为null或0，无法计算内存使用率");
            return 0.0;
        }
        
        memoryUsageRate = Double.parseDouble(DECIMAL_FORMAT.format((double) usedMemory / maxMemory * 100));
        return memoryUsageRate;
    }

    /**
     * 计算内存碎片风险等级
     * 根据内存碎片比率计算风险等级，值越高风险越大
     *
     * @return 内存碎片风险等级
     */
    @Override
    public double calculateFragmentationRiskLevel() {
        if (fragmentationRiskLevel != null) {
            return fragmentationRiskLevel;
        }
        
        if (memFragmentationRatio == null) {
            log.warn("memFragmentationRatio为null，无法计算碎片风险等级");
            return 0.0;
        }
        // 简单的风险等级计算：碎片比率越高，风险等级越高
        fragmentationRiskLevel = Double.parseDouble(DECIMAL_FORMAT.format(memFragmentationRatio * 10));
        return fragmentationRiskLevel;
    }

    /**
     * 计算内存驱逐风险指标
     * 基于驱逐键数量计算风险值
     *
     * @return 内存驱逐风险指标
     */
    @Override
    public double calculateEvictionRiskIndicator() {
        if (evictionRiskIndicator != null) {
            return evictionRiskIndicator;
        }
        
        if (evictedKeys == null) {
            log.warn("evictedKeys为null，无法计算驱逐风险指标");
            return 0.0;
        }
        // 简单的风险指标计算：驱逐键数量越多，风险越高
        evictionRiskIndicator = Double.parseDouble(DECIMAL_FORMAT.format(Math.log10(evictedKeys + 1D) * 10));
        return evictionRiskIndicator;
    }

    /**
     * 显示所有指标信息
     */
    @Override
    public void showAll() {
        log.info("已使用内存: {}B, 转化为MB: {}MB", usedMemory, usedMemoryMb);
        log.info("最大可分配内存: {}B, 转化为MB: {}MB", maxMemory, maxMemoryMb);
        log.info("已使用内存占比: {}%", queryUsedMemoryPercent());
        log.info("内存碎片比率: {}", memFragmentationRatio);
        log.info("因内存不足而被驱逐的键数量: {}", evictedKeys);
        log.info("数据集使用的内存量: {}B, 转化为MB: {}MB", usedMemoryDataset, usedMemoryDatasetMb);
        log.info("Redis进程使用的物理内存总量: {}B, 转化为MB: {}MB", usedMemoryRss, usedMemoryRssMb);
        log.info("高精度内存使用率: {}%", queryMemoryUsageRate());
        log.info("内存碎片风险等级: {}", calculateFragmentationRiskLevel());
        log.info("内存驱逐风险指标: {}", calculateEvictionRiskIndicator());
    }

    /**
     * 监控告警
     * 返回Redis内存监控指标的告警信息
     *
     * @return 告警信息
     */
    @Override
    public String monitorAlerts() {
        // 创建StringBuilder用于收集告警信息
        StringBuilder sb = new StringBuilder();
        
        // 获取各项监控指标值
        int memoryPercent = queryUsedMemoryPercent();
        double fragmentationRisk = calculateFragmentationRiskLevel();
        double evictionRisk = calculateEvictionRiskIndicator();
        
        // 定义告警规则列表，每个规则包含判断条件和告警信息
        List<AlertRule> alertRules = Arrays.asList(
                // 内存使用率过高告警：当内存使用百分比超过阈值时触发
                new AlertRule(() -> memoryPercent > MEMORY_PERCENT_THRESHOLD, "内存使用率过高: " + memoryPercent),
                // 内存碎片风险告警：当内存碎片比率超过阈值时触发
                new AlertRule(() -> memFragmentationRatio > MEM_FRAGMENTATION_RATIO_THRESHOLD, "内存碎片风险: " + memFragmentationRatio),
                // 内存驱逐告警：当存在因内存不足被驱逐的键时触发
                new AlertRule(() -> evictedKeys > 0, "存在因内存不足被驱逐的键数量: " + evictedKeys),
                // 内存碎片风险等级过高告警：当碎片风险等级超过阈值时触发
                new AlertRule(() -> fragmentationRisk > FRAGMENTATION_RISK_THRESHOLD, "内存碎片风险等级过高: " + fragmentationRisk),
                // 内存驱逐风险等级过高告警：当驱逐风险等级超过阈值时触发
                new AlertRule(() -> evictionRisk > EVICTION_RISK_THRESHOLD, "内存驱逐风险等级过高: " + evictionRisk)
        );

        // 遍历告警规则，筛选出满足条件的规则并执行告警信息收集
        alertRules.stream()
                // 筛选触发告警的规则
                .filter(alertRule -> alertRule.getIsAlert().get())
                // 执行告警信息收集
                .forEach(alertRule -> alertRule.showAlert(sb));
        
        // 返回最终的告警信息字符串
        return sb.isEmpty() ? "监控指标正常！" : sb.toString();
    }
}
