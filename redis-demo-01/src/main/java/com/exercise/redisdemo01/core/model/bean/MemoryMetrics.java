package com.exercise.redisdemo01.core.model.bean;

/**
 * 内存监控信息接口
 * 定义了内存监控指标的相关方法，用于监控和分析实例的内存使用情况
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/11
 */
public interface MemoryMetrics {

    /**
     * 查询已使用内存占比
     * 
     * @return 已使用内存的百分比
     */
    int queryUsedMemoryPercent();

    /**
     * 查询高精度的内存使用率
     * 
     * @return 高精度内存使用率
     */
    double queryMemoryUsageRate();

    /**
     * 计算内存碎片风险等级
     * 
     * @return 内存碎片风险等级
     */
    double calculateFragmentationRiskLevel();

    /**
     * 计算内存驱逐风险指标
     * 
     * @return 内存驱逐风险指标
     */
    double calculateEvictionRiskIndicator();

    /**
     * 显示所有指标信息
     */
    void showAll();

    /**
     * 监控告警
     * 
     * @return 告警信息
     */
    String monitorAlerts();
}
