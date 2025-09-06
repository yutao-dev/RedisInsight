package com.exercise.redisdemo01.core.model.bean;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Supplier;


/**
 * 简单规则+message的整合类
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/12
 */
@Data
@AllArgsConstructor
public class AlertRule {
    /**
     * 告警判断条件，通过Supplier函数式接口实现动态判断
     * 返回true表示触发告警，false表示不触发告警
     */
    private Supplier<Boolean> isAlert;

    /**
     * 告警信息内容，当告警条件满足时需要输出的信息
     */
    private String message;

    /**
     * 检查并展示告警信息
     * 当告警条件满足时，将告警信息追加到StringBuilder中
     * 如果StringBuilder中已有内容，则先添加逗号分隔符
     * 
     * @param sb 用于收集告警信息的StringBuilder对象，不可为null
     */
    public void showAlert(StringBuilder sb) {
        if (Boolean.TRUE.equals(isAlert.get())) {
            sb.append(sb.isEmpty() ? "" : ",");
            sb.append(message);
        }
    }
}