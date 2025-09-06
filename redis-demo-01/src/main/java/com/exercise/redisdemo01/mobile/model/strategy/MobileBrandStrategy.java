package com.exercise.redisdemo01.mobile.model.strategy;

/**
 * 手机品牌策略类，专注于针对不同的手机品牌进行不同的策略方式选择
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/12
 */
public interface MobileBrandStrategy {

    /**
     * 手机购买策略
     *
     * @param price     手机价格
     * @param buyResult 手机购买结果
     * @return 手机购买结果
     */
    StringBuilder buyMobile(Integer price, StringBuilder buyResult);

    /**
     * 获取手机品牌
     *
     * @return 手机品牌
     */
    String getBrand();
}
